/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.structureTemplates.internal.systems;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;
import org.terasology.structureTemplates.interfaces.StructureTemplateProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation for {@link StructureTemplateProvider}.
 */
@Share(StructureTemplateProvider.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class StructureTemplateProviderSystem extends BaseComponentSystem implements StructureTemplateProvider {
    private static final Logger logger = LoggerFactory.getLogger(StructureTemplateProviderSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    private Random random = new Random();

    @In
    private AssetManager assetManager;

    private Map<ResourceUrn, List<EntityChanceTuple>> structureTypeToEntitiesMap;

    @Override
    public void postBegin() {
        initIfNotAlreadyDone();
    }

    private void initIfNotAlreadyDone() {
        if (structureTypeToEntitiesMap != null) {
            return;
        }
        Iterable<Prefab> typePrefabs = prefabManager.listPrefabs(StructureTemplateTypeComponent.class);
        structureTypeToEntitiesMap = Maps.newHashMap();
        for (Prefab prefab: typePrefabs) {
            structureTypeToEntitiesMap.put(prefab.getUrn(), new ArrayList<>());
        }

        Iterable<Prefab> prefabs = prefabManager.listPrefabs(StructureTemplateComponent.class);
        for (Prefab prefab: prefabs) {
            StructureTemplateComponent component = prefab.getComponent(StructureTemplateComponent.class);
            Prefab structureTypePrefab = component.type;
            if (structureTypePrefab == null) {
                continue;
            }
            EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
            entityBuilder.setPersistent(false);
            EntityRef entity = entityBuilder.build();
            List<EntityChanceTuple> entityChanceTuples = structureTypeToEntitiesMap.get(structureTypePrefab.getUrn());
            if (entityChanceTuples == null) {
                logger.error(String.format(
                        "The type %s of structue template %s is invalid. The type must be a prefab with the StructureTemplateType component",
                        structureTypePrefab.getUrn(), prefab.getUrn()));
                continue;
            }
            if (component.spawnChance == 0) {
                continue;
            }
            entityChanceTuples.add(new EntityChanceTuple(entity, component.spawnChance));
        }
    }

    private static final class EntityChanceTuple {
        private EntityRef entity;
        private int chance;

        public EntityChanceTuple(EntityRef entity, int chance) {
            this.entity = entity;
            this.chance = chance;
        }

        public int getChance() {
            return chance;
        }

        public EntityRef getEntity() {
            return entity;
        }
    }

    public EntityRef getRandomTemplateOfType(Prefab type) {
        List<EntityChanceTuple> list = getEntityChanceTuplesForPrefab(type);
        return selectRandomOneBasedOnChance(list);
    }

    List<EntityChanceTuple> getEntityChanceTuplesForPrefab(Prefab type) {
        initIfNotAlreadyDone();
        List<EntityChanceTuple> list = structureTypeToEntitiesMap.get(type.getUrn());
        if (list == null) {
            throw new IllegalArgumentException("No valid structure template type: " + type.getUrn());
        }
        if (list.size() == 0) {
            throw new IllegalArgumentException("Structure template type has no templates (with spawn chance > 0): " + type.getUrn());
        }
        return list;
    }

    EntityRef selectRandomOneBasedOnChance(List<EntityChanceTuple> list) {
        EntityChanceTuple entityChanceTuple = list.get(randomIndexBasedOnSpawnChance(list));
        return entityChanceTuple.getEntity();
    }


    private int randomIndexBasedOnSpawnChance(List<EntityChanceTuple> list) {
        long sumOfAll = 0;
        for (EntityChanceTuple entityChanceTuple:list) {
            sumOfAll += entityChanceTuple.getChance();
        }
        long randomValue = Math.abs(random.nextLong() % sumOfAll);
        long sum = 0;
        for (int index = 0;index < list.size(); index++) {
            EntityChanceTuple entityChanceTuple = list.get(index);
            sum += entityChanceTuple.getChance();
            if (randomValue < sum) {
                return index;
            }
        }
        throw new IllegalArgumentException("list had 0 spawn chance");
    }

    public EntityRef getRandomTemplateOfType(String structureTemplateTypePrefab) {
        Optional<Prefab> prefab = assetManager.getAsset(structureTemplateTypePrefab, Prefab.class);
        if (!prefab.isPresent()) {
            throw new IllegalArgumentException("The provided argument is not a prefab: " + structureTemplateTypePrefab);
        }
        return getRandomTemplateOfType(prefab.get());
    }

    public Iterator<EntityRef> iterateStructureTempaltesOfTypeInRandomOrder(Prefab prefab) {
        List<EntityChanceTuple> entityChanceTuples = getEntityChanceTuplesForPrefab(prefab);
        return new StructureTemplateIterator(entityChanceTuples);
    }

    private class StructureTemplateIterator implements Iterator<EntityRef> {
        private List<EntityChanceTuple> remaining;
        /**
         *
         * @param list won't be modified, this class works with a copy of this list
         */
        public StructureTemplateIterator(List<EntityChanceTuple> list) {
            this.remaining = new ArrayList<EntityChanceTuple>(list);
        }

        @Override
        public boolean hasNext() {
            return !remaining.isEmpty();
        }

        @Override
        public EntityRef next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int ramdomIndex = randomIndexBasedOnSpawnChance(remaining);
            EntityChanceTuple entityChanceTuple = remaining.remove(ramdomIndex);
            return entityChanceTuple.getEntity();
        }
    }
}
