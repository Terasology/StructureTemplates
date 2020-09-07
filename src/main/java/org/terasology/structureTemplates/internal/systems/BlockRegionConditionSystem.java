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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.Region3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.BlockPredicateComponent;
import org.terasology.structureTemplates.components.CheckBlockRegionConditionComponent;
import org.terasology.structureTemplates.components.CheckBlockRegionConditionComponent.BlockRegionConditionCheck;
import org.terasology.structureTemplates.components.RequiredBlockPropertiesComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;
import org.terasology.structureTemplates.interfaces.BlockPredicateProvider;
import org.terasology.structureTemplates.interfaces.BlockRegionChecker;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * System powering  {@link CheckBlockRegionConditionComponent}.
 *
 * Includes also the event handling for {@link RequiredBlockPropertiesComponent}.
 *
 */
@Share({BlockRegionChecker.class, BlockPredicateProvider.class})
@RegisterSystem(RegisterMode.ALWAYS)
public class BlockRegionConditionSystem extends BaseComponentSystem implements BlockRegionChecker,
        BlockPredicateProvider {


    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @In
    private BlockManager blockManager;

    private Map<ResourceUrn, EntityRef> prefabUrnToEntityMap = new HashMap<>();

    @Override
    public boolean allBlocksMatch(Region3i untransformedRegion, BlockRegionTransform transform, Predicate<Block> condition) {
        Region3i region = transform.transformRegion(untransformedRegion);
        return allBlocksInAABBMatch(region.minX(), region.maxX(), region.minY(), region.maxY(), region.minZ(),
                region.maxZ(), condition, transform);
    }

    private boolean allBlocksInAABBMatch(int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                         Predicate<Block> condition,  BlockRegionTransform transform) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block untransformedBlock = worldProvider.getBlock(x ,y, z);
                    Block transformedBlock = transform.transformBlock(untransformedBlock);
                    if (!condition.test(transformedBlock)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    @Override
    public void postBegin() {
        Collection<Prefab> conditionPrefabs = prefabManager.listPrefabs(BlockPredicateComponent.class);
        for (Prefab conditionPrefab: conditionPrefabs) {
            createEntityForPrefab(conditionPrefab);
        }
    }



    @ReceiveEvent
    public void onCheckSpawnConditionEvent(CheckSpawnConditionEvent event, EntityRef entity,
                                          CheckBlockRegionConditionComponent conditionComponent) {
        for (BlockRegionConditionCheck checkToPerform: conditionComponent.checksToPerform) {
            Prefab conditionPrefab = checkToPerform.condition;
            if (conditionPrefab == null) {
                return;
            }
            Region3i relativeRegion = checkToPerform.region;
            if (!allBlocksMatch(relativeRegion,  event.getBlockRegionTransform(), conditionPrefab)) {
                event.setPreventSpawn(true);
                Region3i absoluteRegion = event.getBlockRegionTransform().transformRegion(relativeRegion);
                event.setSpawnPreventingRegion(absoluteRegion);
                event.setFailedSpawnCondition(conditionPrefab);
                event.consume();
                return;
            }
        }
    }

    public boolean allBlocksMatch(Region3i untransformedRegion, BlockRegionTransform transform, Prefab prefab) {
        Predicate<Block> predicate = getBlockPredicate(prefab);
        return allBlocksMatch(untransformedRegion, transform, predicate);
    }

    @Override
    public Predicate<Block> getBlockPredicate(Prefab conditionPrefab) {
        EntityRef conditionEntity = prefabUrnToEntityMap.get(conditionPrefab.getUrn());
        if (conditionEntity == null) {
            if (conditionPrefab.hasComponent(BlockPredicateComponent.class)) {
                conditionEntity = createEntityForPrefab(conditionPrefab);
            } else {
                throw new IllegalArgumentException(String.format("Prefab %s does not have the component %s",
                        conditionPrefab.getName(), BlockPredicateComponent.class.getSimpleName()));
            }
        }
        GetBlockPredicateEvent getBlockPredicateEvent = new GetBlockPredicateEvent();
        conditionEntity.send(getBlockPredicateEvent);
        return getBlockPredicateEvent.predicate;
    }

    @Override
    public Predicate<Block> getBlockPredicate(String name) throws IllegalArgumentException {
        Prefab prefab = prefabManager.getPrefab(name);
        if (prefab == null) {
            throw new IllegalArgumentException("Can't find prefab with name " + name);
        }
        return getBlockPredicate(prefab);
    }

    private EntityRef createEntityForPrefab(Prefab conditionPrefab) {
        EntityRef entityRef = entityManager.create(conditionPrefab);
        prefabUrnToEntityMap.put(conditionPrefab.getUrn(), entityRef);
        return entityRef;
    }

    @ReceiveEvent
    public void onGetBlockPropertiesPredicate(GetBlockPredicateEvent event, EntityRef entity,
                                        RequiredBlockPropertiesComponent requiredBlockPropertiesComponent) {
        final Boolean wantedLiquidValue = requiredBlockPropertiesComponent.liquid;
        if (wantedLiquidValue != null) {
            Predicate<Block> condition = (block) -> (block.isLiquid() == wantedLiquidValue.booleanValue());
            event.predicate = event.predicate.and(condition);
        }
        final Boolean wantedPenetrableValue = requiredBlockPropertiesComponent.penetrable;
        if (wantedPenetrableValue != null) {
            Predicate<Block> condition = (block) -> (block.isPenetrable() == wantedPenetrableValue.booleanValue());
            event.predicate = event.predicate.and(condition);
        }
        final Boolean wantedLoadedValue = requiredBlockPropertiesComponent.loaded;
        if (wantedLoadedValue != null) {
            Predicate<Block> condition = (block) -> ((!block.getURI().equals(BlockManager.UNLOADED_ID))
                    == wantedLoadedValue.booleanValue());
            event.predicate = event.predicate.and(condition);
        }
    }
}

