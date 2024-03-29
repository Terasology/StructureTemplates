// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
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

    private final Random random = new Random();

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
        for (int index = 0; index < list.size(); index++) {
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

    public Iterator<EntityRef> iterateStructureTemplatesOfTypeInRandomOrder(Prefab prefab) {
        List<EntityChanceTuple> entityChanceTuples = getEntityChanceTuplesForPrefab(prefab);
        return new StructureTemplateIterator(entityChanceTuples);
    }

    private static final class EntityChanceTuple {
        private final EntityRef entity;
        private final int chance;

        EntityChanceTuple(EntityRef entity, int chance) {
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

    private class StructureTemplateIterator implements Iterator<EntityRef> {
        private List<EntityChanceTuple> remaining;
        /**
         *
         * @param list won't be modified, this class works with a copy of this list
         */
        StructureTemplateIterator(List<EntityChanceTuple> list) {
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
