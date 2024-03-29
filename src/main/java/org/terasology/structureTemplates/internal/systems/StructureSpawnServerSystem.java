// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.structureTemplates.components.CompletionTimeComponent;
import org.terasology.structureTemplates.components.IgnoreAirBlocksComponent;
import org.terasology.structureTemplates.components.NoConstructionAnimationComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksForMidAirEvent;
import org.terasology.structureTemplates.events.SpawnBlocksOfStructureTemplateEvent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.events.StructureSpawnStartedEvent;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent.BlockToPlace;
import org.terasology.structureTemplates.internal.components.BuildStepwiseStructureComponent.BuildStep;
import org.terasology.structureTemplates.internal.components.BuildStructureCounterComponent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Priority(EventPriority.PRIORITY_TRIVIAL)
    @ReceiveEvent
    public void onSpawnStructureWithoutFallingAnimation(SpawnStructureEvent event, EntityRef entity) {
        regionTransform = event.getTransformation();
        structureEntity = entity;
        entity.send(new StructureSpawnStartedEvent(event.getTransformation()));
        entity.send(new SpawnBlocksOfStructureTemplateEvent(event.getTransformation()));
        event.consume();
    }

    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent
    public void onSpawnStructureWithNoAnimation(SpawnStructureEvent event,
                                                EntityRef entity,
                                                SpawnBlockRegionsComponent spawnBlockRegionsComponent,
                                                NoConstructionAnimationComponent noConstructionAnimationComponent) {
        BlockRegionTransform transformation = event.getTransformation();

        Map<Vector3ic, Block> blocksToPlace = Maps.newHashMap();

        for (RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
            Block block = regionToFill.blockType;
            if (entity.hasComponent(IgnoreAirBlocksComponent.class) && isAir(block)) {
                continue;
            }

            BlockRegion region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            for (Vector3ic pos : region) {
                blocksToPlace.put(new Vector3i(pos), block);
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

    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent
    public void onSpawnTemplateEventWithBlocksPriority(SpawnTemplateEvent event, EntityRef entity) {
        structureEntity = entity;
        entity.send(new SpawnBlocksOfStructureTemplateEvent(event.getTransformation()));
    }

    @ReceiveEvent
    public void onGetStructureTemplateBlocks(GetStructureTemplateBlocksEvent event, EntityRef entity,
                                             SpawnBlockRegionsComponent spawnBlockRegionComponent) {

        BlockRegionTransform transformation = event.getTransformation();

        Map<Integer, List<BlockToPlace>> blocksPerLayer = Maps.newTreeMap();
        for (RegionToFill regionToFill : spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;
            if (entity.hasComponent(IgnoreAirBlocksComponent.class) && isAir(block)) {
                continue;
            }

            BlockRegion region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            for (Vector3ic pos : region) {
                final int y = pos.y();
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
            if (entity.hasComponent(IgnoreAirBlocksComponent.class) && isAir(block)) {
                continue;
            }

            BlockRegion region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);


            event.fillRegion(region, block);
        }
    }


    @ReceiveEvent
    public void onDelayedTriggeredEvent(DelayedActionTriggeredEvent event,
                                        EntityRef entity,
                                        BuildStepwiseStructureComponent buildStepwiseStructureComponent,
                                        BuildStructureCounterComponent counterComponent) {

        if (!event.getActionId().equals(GROW_STRUCTURE_ACTION_ID)) {
            return;
        }
        int currentStepCount = counterComponent.iter;
        List<BuildStep> buildSteps = buildStepwiseStructureComponent.getBuildSteps();
        BuildStep step = buildSteps.get(currentStepCount);

        Map<Vector3ic, Block> blocksToPlace = Maps.newHashMap();

        for (BlockToPlace blockToPlace : step.blocksInStep) {
            blocksToPlace.put(blockToPlace.pos, blockToPlace.block);
        }

        worldProvider.setBlocks(blocksToPlace); //TODO: finish migration

        if (currentStepCount + 1 < buildSteps.size()) {
            counterComponent.iter = currentStepCount + 1;
            entity.saveComponent(counterComponent);
            delayManager.addDelayedAction(entity, GROW_STRUCTURE_ACTION_ID, 1000);
        } else {
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

    private boolean isAir(final Block block) {
        return block.getURI().getBlockFamilyDefinitionUrn().equals(BlockManager.AIR_ID.getBlockFamilyDefinitionUrn());
    }

    // TODO move method into utility class:
    public static BlockRegionTransform getBlockRegionTransformForStructurePlacement(ActivateEvent event,
                                                                                    BlockComponent blockComponent) {
        LocationComponent characterLocation = event.getInstigator().getComponent(LocationComponent.class);
        Vector3f directionVector = characterLocation.getWorldDirection(new Vector3f());

        Side facedDirection = Side.inHorizontalDirection(directionVector.x(), directionVector.z());
        Side wantedFrontOfStructure = facedDirection.reverse();

        return createBlockRegionTransformForCharacterTargeting(Side.FRONT,
            wantedFrontOfStructure, blockComponent.getPosition(new Vector3i()));
    }

    public static BlockRegionTransform createBlockRegionTransformForCharacterTargeting(
        Side fromSide, Side toSide, Vector3i target) {
        return BlockRegionTransform.createRotationThenMovement(fromSide, toSide, target);
    }

    private static BlockToPlace createBlockToPlace(Vector3ic pos, Block block) {
        BlockToPlace b = new BlockToPlace();
        b.pos = new Vector3i(pos);
        b.block = block;
        return b;
    }

}
