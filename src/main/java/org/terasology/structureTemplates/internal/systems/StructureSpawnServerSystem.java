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

import com.google.common.collect.Lists;

import java.util.Map;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.CompletionTimeComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.SpawnBlocksOfStructureTemplateEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksForMidAirEvent;
import org.terasology.structureTemplates.events.StructureSpawnStartedEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent.BlockToPlace;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent.BuildStep;
import org.terasology.structureTemplates.internal.components.BuildStructureCounterComponent;
import org.terasology.structureTemplates.components.NoConstructionAnimationComponent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.structureTemplates.components.IgnoreAirBlocksComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;


/**
 * Spawns structures when entities with certain components receive a {@link SpawnStructureEvent}.
 * e.g. the entity that receives a {@link SpawnStructureEvent} has a {@link SpawnBlockRegionsComponent} then
 * the regions specified by that component will be filled with the specified block types.
 * <p>
 * Handles also the activation of items with the {@link SpawnStructureActionComponent}
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class StructureSpawnServerSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(StructureSpawnServerSystem.class);

    private static final String GROW_STRUCTURE_ACTION_ID = "structureTemplates:growStructureEvent";
    private static final String CONSTRUCTION_COMPLETE_ACTION_ID = "structureTemplates:structureCompleteEvent";

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private BlockManager blockManager;

    @In
    private AssetManager assetManager;

    private BlockRegionTransform regionTransform;
    private EntityRef structureEntity;

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onSpawnStructureWithoutFallingAnimation(SpawnStructureEvent event, EntityRef entity) {
        regionTransform = event.getTransformation();
        structureEntity = entity;
        entity.send(new StructureSpawnStartedEvent(event.getTransformation()));
        entity.send(new SpawnBlocksOfStructureTemplateEvent(event.getTransformation()));
        event.consume();
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onSpawnStructureWithNoAnimation(SpawnStructureEvent event,
                                                EntityRef entity,
                                                SpawnBlockRegionsComponent spawnBlockRegionsComponent,
                                                NoConstructionAnimationComponent noConstructionAnimationComponent) {
        BlockRegionTransform transformation = event.getTransformation();

        Map<Vector3i, Block> blocksToPlace = Maps.newHashMap();

        for (RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
            Block block = regionToFill.blockType;
            if(entity.hasComponent(IgnoreAirBlocksComponent.class)&&block.getURI().getBlockFamilyDefinitionUrn().equals(BlockManager.AIR_ID.getBlockFamilyDefinitionUrn()))
            {
                continue;
            }
                Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            for (Vector3i pos : region) {
                blocksToPlace.put(pos, block);
            }
        }

        worldProvider.setBlocks(blocksToPlace);

        event.consume();
    }

    @ReceiveEvent
    public void onSpawnBlocksOfStructureTemplateEvent(SpawnBlocksOfStructureTemplateEvent event, EntityRef entity) {
        long startTime = System.currentTimeMillis();
        GetStructureTemplateBlocksEvent getBlocksEvent = new GetStructureTemplateBlocksEvent(event.getTransformation());
        entity.send(getBlocksEvent);
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        if (delta > 20) {
            logger.warn("Structure of type {} took {} ms to spawn", entity.getParentPrefab().getName(), delta);
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onSpawnTemplateEventWithBlocksPriority(SpawnTemplateEvent event, EntityRef entity) {
        entity.send(new SpawnBlocksOfStructureTemplateEvent(event.getTransformation()));
    }

    @ReceiveEvent
    public void onGetStructureTemplateBlocks(GetStructureTemplateBlocksEvent event, EntityRef entity,
                                             SpawnBlockRegionsComponent spawnBlockRegionComponent) {

        BlockRegionTransform transformation = event.getTransformation();

        Map<Integer, List<BlockToPlace>> blocksPerLayer = Maps.newTreeMap();
        for (RegionToFill regionToFill : spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;
            if(entity.hasComponent(IgnoreAirBlocksComponent.class)&&block.getURI().getBlockFamilyDefinitionUrn().equals(BlockManager.AIR_ID.getBlockFamilyDefinitionUrn()))
            {
                continue;
            }
                Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            for (Vector3i pos : region) {
                final int y = pos.getY();
                if (!blocksPerLayer.containsKey(y)) {
                    blocksPerLayer.put(y, Lists.newArrayList());
                }
                blocksPerLayer.get(y).add(createBlockToPlace(pos, block));
            }
        }

        List<BuildStep> blocksPerStep = Lists.newArrayList(blocksPerLayer.values()).stream().map(BuildStep::new).collect(Collectors.toList());
        BuildStepwiseStructureComponent buildStepwiseStructureComponent = new BuildStepwiseStructureComponent(blocksPerStep);
        BuildStructureCounterComponent growStructureCounter = new BuildStructureCounterComponent();

        EntityRef growingStructureEntity = entityManager.create(buildStepwiseStructureComponent, growStructureCounter);

        CompletionTimeComponent completionTimeComponent = new CompletionTimeComponent();
        completionTimeComponent.completionDelay = buildStepwiseStructureComponent.getBuildSteps().size() * 1000 + 100;
        structureEntity.addComponent(completionTimeComponent);
        delayManager.addDelayedAction(growingStructureEntity, GROW_STRUCTURE_ACTION_ID, 0);
    }

    @ReceiveEvent
    public void onGetStructureTemplateBlocksMidAir(GetStructureTemplateBlocksForMidAirEvent event, EntityRef entity,
                                                   SpawnBlockRegionsComponent spawnBlockRegionComponent) {

        BlockRegionTransform transformation = event.getTransformation();
        for (RegionToFill regionToFill : spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;
            if(entity.hasComponent(IgnoreAirBlocksComponent.class)&&block.getURI().getBlockFamilyDefinitionUrn().equals(BlockManager.AIR_ID.getBlockFamilyDefinitionUrn()))
            {
                continue;
            }

            Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);


            event.fillRegion(region, block);
        }
    }


    @ReceiveEvent
    public void onDelayedTriggeredEvent(DelayedActionTriggeredEvent event, EntityRef entity,
                                        BuildStepwiseStructureComponent buildStepwiseStructureComponent, BuildStructureCounterComponent counterComponent) {

        if (!event.getActionId().equals(GROW_STRUCTURE_ACTION_ID)) {
            return;
        }
        int currentStepCount = counterComponent.iter;
        List<BuildStep> buildSteps = buildStepwiseStructureComponent.getBuildSteps();
        BuildStep step = buildSteps.get(currentStepCount);

        Map<Vector3i, Block> blocksToPlace = Maps.newHashMap();

        for (BlockToPlace blockToPlace : step.blocksInStep) {
            blocksToPlace.put(blockToPlace.pos, blockToPlace.block);
        }

        worldProvider.setBlocks(blocksToPlace);

        if (currentStepCount + 1 < buildSteps.size()) {
            counterComponent.iter = currentStepCount + 1;
            entity.saveComponent(counterComponent);
            delayManager.addDelayedAction(entity, GROW_STRUCTURE_ACTION_ID, 1000);
        }
        else {
            delayManager.addDelayedAction(entity, CONSTRUCTION_COMPLETE_ACTION_ID, 100);
        }
    }

    @ReceiveEvent
    public void setConstructionComplete(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (!event.getActionId().equals(CONSTRUCTION_COMPLETE_ACTION_ID)) {
            return;
        }

        structureEntity.send(new StructureBlocksSpawnedEvent(regionTransform));
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity,
                           SpawnStructureActionComponent spawnActionComponent,
                           StructureTemplateComponent structureTemplateComponent) {
        // activation with error hides error:
        if (spawnActionComponent.unconfirmSpawnErrorRegion != null) {
            spawnActionComponent.unconfirmSpawnErrorRegion = null;
            entity.saveComponent(spawnActionComponent);
            return;
        }

        EntityRef target = event.getTarget();
        BlockComponent blockComponent = target.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }

        BlockRegionTransform blockRegionTransform = getBlockRegionTransformForStructurePlacement(event,
                blockComponent);
        CheckSpawnConditionEvent checkSpawnEvent = new CheckSpawnConditionEvent(blockRegionTransform);
        entity.send(checkSpawnEvent);
        if (checkSpawnEvent.isPreventSpawn()) {
            spawnActionComponent.unconfirmSpawnErrorRegion = checkSpawnEvent.getSpawnPreventingRegion();
            entity.saveComponent(spawnActionComponent);
            entity.send(new StructureSpawnFailedEvent(checkSpawnEvent.getFailedSpawnCondition(),
                    checkSpawnEvent.getSpawnPreventingRegion()));
            return;
        }

        entity.send(new SpawnStructureEvent(blockRegionTransform));

    }

    // TODO move method into utility class:
    public static BlockRegionTransform getBlockRegionTransformForStructurePlacement(ActivateEvent event,
                                                                                    BlockComponent blockComponent) {
        LocationComponent characterLocation = event.getInstigator().getComponent(LocationComponent.class);
        Vector3f directionVector = characterLocation.getWorldDirection();

        Side facedDirection = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side wantedFrontOfStructure = facedDirection.reverse();

        return createBlockRegionTransformForCharacterTargeting(Side.FRONT,
                wantedFrontOfStructure, blockComponent.getPosition());
    }

    public static BlockRegionTransform createBlockRegionTransformForCharacterTargeting(
            Side fromSide, Side toSide, Vector3i target) {
        return BlockRegionTransform.createRotationThenMovement(fromSide, toSide, target);
    }

    private static BlockToPlace createBlockToPlace(Vector3i pos, Block block) {
        BlockToPlace b = new BlockToPlace();
        b.pos = pos;
        b.block = block;
        return b;
    }

}
