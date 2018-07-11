/*
 * Copyright 2017 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.BlockRegionTransformComponent;
import org.terasology.structureTemplates.components.FallingBlockComponent;
import org.terasology.structureTemplates.components.FallingBlocksPlacementAlgorithmComponent;
import org.terasology.structureTemplates.components.TimedStructureCompletionComponent;
import org.terasology.structureTemplates.events.*;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class FallingBlockPlacementServerSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(FallingBlockPlacementServerSystem.class);
    private static final String COMPLETE_STRUCTURE_ACTION_ID = "StructureTemplates:FallingBlockPlacementServerSystem:completeStructure";
    private static final String PLACE_BLOCK_ACTION_ID = "StructureTemplates:FallingBlockPlacementServerSystem:placeBlock";
    private static final String DESTROY_ENTITY_ACTION_ID = "StructureTemplates:FallingBlockPlacementServerSystem:destroyEntity";
    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private DelayManager delayManager;

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider worldProvider;

    /**
     * This overrides the normal instant structure template spawning with one where an animation of falling blocks
     * gets played. The event {@link StructureSpawnStartedEvent} still gets sent at the start and
     * the event {@link StructureBlocksSpawnedEvent} gets send when the fall down animation has been played for
     * all blocks. So there might be a few seconds between the events.
     */
    @ReceiveEvent(components = {FallingBlocksPlacementAlgorithmComponent.class}, priority = EventPriority.PRIORITY_CRITICAL)
    public void onSpawnStructureEventWithBlocksPriority(SpawnStructureEvent event, EntityRef entity) {
        entity.send(new StructureSpawnStartedEvent(event.getTransformation()));
        GetStructureTemplateBlocksForMidAirEvent getBlocksEvent =  new GetStructureTemplateBlocksForMidAirEvent(event.getTransformation());
        entity.send(getBlocksEvent);
        Map<Vector3i, Block> blocksToPlace = getBlocksEvent.getBlocksToPlace();

        replacePlacementLocationWithAir(blocksToPlace.keySet());

        List<EntityRef> fallingBlockEntities = createFallingBlockEntities(blocksToPlace);

        long maxStopGameTimeInMs = determineMaxStopTimeInMs(fallingBlockEntities);
        createEntityThatTriggersTheCompletionEvent(entity, event.getTransformation(), maxStopGameTimeInMs);
        scheduleCleanupOfFallingBlockEntities(fallingBlockEntities, maxStopGameTimeInMs);

        event.consume();
    }

    void scheduleCleanupOfFallingBlockEntities(List<EntityRef> fallingBlockEntities, long maxStopGameTimeInMs) {
        for (EntityRef fallingBlockEntity: fallingBlockEntities) {
            delayManager.addDelayedAction(fallingBlockEntity, DESTROY_ENTITY_ACTION_ID, maxStopGameTimeInMs - time.getGameTimeInMs());
        }
    }

    List<EntityRef> createFallingBlockEntities(Map<Vector3i, Block> blocksToPlace) {
        Vector3i minPos = getSmallestPlacementPosition(blocksToPlace);
        List<EntityRef> fallingBlockEntities = new ArrayList<>();
        for (Map.Entry<Vector3i, Block> entry: blocksToPlace.entrySet()) {
            if (!entry.getValue().getURI().equals(BlockManager.AIR_ID)) {
                EntityRef fallingBlockEntity = createFallingBlockEntity(entry.getKey(), entry.getValue(), minPos);
                fallingBlockEntities.add(fallingBlockEntity);
            }
        }
        return fallingBlockEntities;
    }

    Vector3i getSmallestPlacementPosition(Map<Vector3i, Block> blocksToPlace) {
        Vector3i minPos = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Map.Entry<Vector3i, Block> entry: blocksToPlace.entrySet()) {
            minPos.min(entry.getKey());
        }
        return minPos;
    }

    long determineMaxStopTimeInMs(List<EntityRef> fallingBlockEntities) {
        long maxStopGameTimeInMs = time.getGameTimeInMs();
        for (EntityRef fallingBlockEntity: fallingBlockEntities) {
            FallingBlockComponent component = fallingBlockEntity.getComponent(FallingBlockComponent.class);
            maxStopGameTimeInMs = Math.max(maxStopGameTimeInMs, component.stopGameTimeInMs);
        }
        return maxStopGameTimeInMs;
    }

    private EntityRef createEntityThatTriggersTheCompletionEvent(EntityRef structureTemplate, BlockRegionTransform transform,
                                                                 long maxStopGameTimeInMs) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(true);
        entityBuilder.addComponent(new NetworkComponent());

        LocationComponent locationComponent = new LocationComponent();
        locationComponent.setWorldPosition(transform.transformVector3i(new Vector3i(0,0,0)).toVector3f());
        entityBuilder.addComponent(locationComponent);

        TimedStructureCompletionComponent timedStructureCompletionComponent = new TimedStructureCompletionComponent();
        timedStructureCompletionComponent.gameTimeInMsWhenStructureGetsCompleted = maxStopGameTimeInMs;
        timedStructureCompletionComponent.structureTemplate = structureTemplate;
        entityBuilder.addComponent(timedStructureCompletionComponent);


        entityBuilder.addComponent(transform.toComponent());

        EntityRef entityRef = entityBuilder.build();
        long durationTillLastBlockPlacement = maxStopGameTimeInMs - time.getGameTimeInMs();
        // Add 1ms to ensure it is indeed after the last block placement.
        long directlyAfterLastBlockPlacement = durationTillLastBlockPlacement +5;
        delayManager.addDelayedAction(entityRef, COMPLETE_STRUCTURE_ACTION_ID, directlyAfterLastBlockPlacement);
        return entityRef;
    }

    @ReceiveEvent
    public void onDelayedStructureCompletionTrigger(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                   TimedStructureCompletionComponent completionComponent,
                                   BlockRegionTransformComponent transformComponent) {
        if (!event.getActionId().equals(COMPLETE_STRUCTURE_ACTION_ID)) {
            return;
        }
        EntityRef structureTemplate = completionComponent.structureTemplate;
        BlockRegionTransform transform = BlockRegionTransform.createFromComponent(transformComponent);
        structureTemplate.send(new SpawnBlocksOfStructureTemplateEvent(transform));
        structureTemplate.send(new StructureBlocksSpawnedEvent(transform));

        entityRef.destroy();
    }


    private void replacePlacementLocationWithAir(Set<Vector3i> locations) {
        Map<Vector3i, Block> airBlocksToPlace = new HashMap<>();
        Block air = blockManager.getBlock(BlockManager.AIR_ID);
        for (Vector3i position:locations) {
            airBlocksToPlace.put(position, air);
        }
        worldProvider.setBlocks(airBlocksToPlace);
    }

    private EntityRef createFallingBlockEntity(Vector3i targetPosition, Block block, Vector3i minPosOfAllBlocks) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(true);
        entityBuilder.addComponent(new NetworkComponent());


        LocationComponent locationComponent = new LocationComponent();
        float distanceToMin = targetPosition.gridDistance(minPosOfAllBlocks);
        float additionalOffsetPerDistanceToMin = 1f;
        float fallDistance = additionalOffsetPerDistanceToMin * distanceToMin;
        locationComponent.setWorldPosition(targetPosition.toVector3f());
        entityBuilder.addComponent(locationComponent);

        long fallDurationInMs = Math.round(Math.sqrt(2*fallDistance / (-FallingBlockPlacementClientSystem.FALLING_BLOCK_ACCELERATION_IN_M_PER_MS)));

        FallingBlockComponent fallingBlockComponent = new FallingBlockComponent();
        fallingBlockComponent.startGameTimeInMs = time.getGameTimeInMs();
        fallingBlockComponent.stopGameTimeInMs = fallingBlockComponent.startGameTimeInMs  + fallDurationInMs;
        fallingBlockComponent.blockUri = block.getURI().toString();
        entityBuilder.addComponent(fallingBlockComponent);

        EntityRef entity = entityBuilder.build();
        //delayManager.addDelayedAction(entity, PLACE_BLOCK_ACTION_ID, fallDurationInMs);
        return entity;
    }

    @ReceiveEvent
    public void onFallingBlockDelayElapsed(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                                    FallingBlockComponent component, LocationComponent locationComponent) {
        if (event.getActionId().equals(PLACE_BLOCK_ACTION_ID)) {

            Vector3f pos = locationComponent.getWorldPosition();
            Block block = blockManager.getBlock(component.blockUri);
            if (block == null) {
                logger.error("Block with url not found, block placement of fallen block skipped: " + Objects.toString(component.blockUri));
            }
            Vector3i roundedPos = new Vector3i(Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ()));
            logger.info("Placing block at " + roundedPos);
            worldProvider.setBlock(roundedPos, block);
        }
        if (event.getActionId().equals(DESTROY_ENTITY_ACTION_ID)) {
            entityRef.destroy();
        }
    }

}
