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
import org.terasology.structureTemplates.components.CompletionTimeComponent;
import org.terasology.structureTemplates.components.PrepareFallingBlockEntityComponent;
import org.terasology.structureTemplates.components.FallingBlockComponent;
import org.terasology.structureTemplates.components.FallingBlocksPlacementAlgorithmComponent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksForMidAirEvent;
import org.terasology.structureTemplates.events.StructureSpawnStartedEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.HashMap;
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
    private static final String CREATE_ENTITY_ACTION_ID = "StructureTemplates:FallingBlockPlacementServerSystem:createEntity";
    private static final String COMPLETE_CREATE_ENTITY_ACTION_ID = "StructureTemplates:FallingBlockPlacementServerSystem:completeCreateEntity";

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

    long maxStopGameTimeInMs = 0;
    BlockRegionTransform transformation;

    /**
     * This overrides the normal instant structure template spawning with one where an animation of falling blocks
     * gets played. The event {@link StructureSpawnStartedEvent} still gets sent at the start and
     * the event {@link StructureBlocksSpawnedEvent} gets send when the fall down animation has been played for
     * all blocks. So there might be a few seconds between the events.
     */
    @ReceiveEvent(components = {FallingBlocksPlacementAlgorithmComponent.class})
    public void onSpawnStructureEventWithBlocksPriority(SpawnStructureEvent event, EntityRef entity) {
        transformation = event.getTransformation();
        entity.send(new StructureSpawnStartedEvent(event.getTransformation()));
        GetStructureTemplateBlocksForMidAirEvent getBlocksEvent =  new GetStructureTemplateBlocksForMidAirEvent(event.getTransformation());
        entity.send(getBlocksEvent);
        Map<Vector3i, Block> blocksToPlace = getBlocksEvent.getBlocksToPlace();

        replacePlacementLocationWithAir(blocksToPlace.keySet());

        createFallingBlockEntities(blocksToPlace, entity);
        event.consume();
    }

    void createFallingBlockEntities(Map<Vector3i, Block> blocksToPlace, EntityRef entityRef) {
        Vector3i minPos = getSmallestPlacementPosition(blocksToPlace);
        long delay = 0;
        for (Map.Entry<Vector3i, Block> entry: blocksToPlace.entrySet()) {
            if (!entry.getValue().getURI().equals(BlockManager.AIR_ID)) {

                float distanceToMin = entry.getKey().gridDistance(minPos);
                float additionalOffsetPerDistanceToMin = 1f;
                float fallDistance = additionalOffsetPerDistanceToMin * distanceToMin;
                long fallDurationInMs = Math.round(Math.sqrt(2 * fallDistance / (-FallingBlockPlacementClientSystem.FALLING_BLOCK_ACCELERATION_IN_M_PER_MS)));

                delay += fallDurationInMs;

                PrepareFallingBlockEntityComponent prepareBlockEntityComponent = new PrepareFallingBlockEntityComponent();
                prepareBlockEntityComponent.block = entry.getValue();
                prepareBlockEntityComponent.targetPosition = entry.getKey();
                prepareBlockEntityComponent.fallDurationInMs = fallDurationInMs;

                EntityRef entity = entityManager.create(prepareBlockEntityComponent);
                delayManager.addDelayedAction(entity, CREATE_ENTITY_ACTION_ID, delay);
            }
        }
        CompletionTimeComponent completionTimeComponent = new CompletionTimeComponent();
        completionTimeComponent.completionDelay = delay + 100;
        entityRef.addComponent(completionTimeComponent);

        delayManager.addDelayedAction(entityRef, COMPLETE_CREATE_ENTITY_ACTION_ID, delay + 100);
    }

    Vector3i getSmallestPlacementPosition(Map<Vector3i, Block> blocksToPlace) {
        Vector3i minPos = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Map.Entry<Vector3i, Block> entry: blocksToPlace.entrySet()) {
            minPos.min(entry.getKey());
        }
        return minPos;
    }

    private void createEntityThatTriggersTheCompletionEvent(EntityRef structureTemplate, BlockRegionTransform transform) {
        /**EntityBuilder entityBuilder = entityManager.newBuilder();
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

        EntityRef entityRef = entityBuilder.build();**/
        delayManager.addDelayedAction(structureTemplate, COMPLETE_STRUCTURE_ACTION_ID, 0);
    }

    @ReceiveEvent
    public void onDelayedStructureCompletionTrigger(DelayedActionTriggeredEvent event, EntityRef structureTemplate) {
        if (!event.getActionId().equals(COMPLETE_STRUCTURE_ACTION_ID)) {
            return;
        }
        //structureTemplate.send(new SpawnBlocksOfStructureTemplateEvent(transform));

        structureTemplate.send(new StructureBlocksSpawnedEvent(transformation));
    }


    private void replacePlacementLocationWithAir(Set<Vector3i> locations) {
        Map<Vector3i, Block> airBlocksToPlace = new HashMap<>();
        Block air = blockManager.getBlock(BlockManager.AIR_ID);
        for (Vector3i position:locations) {
            airBlocksToPlace.put(position, air);
        }
        worldProvider.setBlocks(airBlocksToPlace);
    }

    @ReceiveEvent
    public void createFallingBlockEntity(DelayedActionTriggeredEvent event, EntityRef entityRef, PrepareFallingBlockEntityComponent fallingBlockEntityComponent) {
        if (!event.getActionId().equals(CREATE_ENTITY_ACTION_ID)) {
            return;
        }
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(true);
        entityBuilder.addComponent(new NetworkComponent());


        LocationComponent locationComponent = new LocationComponent();
        locationComponent.setWorldPosition(fallingBlockEntityComponent.targetPosition.toVector3f());
        entityBuilder.addComponent(locationComponent);

        long fallDurationInMs = fallingBlockEntityComponent.fallDurationInMs;

        FallingBlockComponent fallingBlockComponent = new FallingBlockComponent();
        fallingBlockComponent.startGameTimeInMs = time.getGameTimeInMs();
        fallingBlockComponent.stopGameTimeInMs = fallingBlockComponent.startGameTimeInMs  + fallDurationInMs;

        maxStopGameTimeInMs = Math.max(maxStopGameTimeInMs, fallingBlockComponent.stopGameTimeInMs);

        fallingBlockComponent.blockUri = fallingBlockEntityComponent.block.getURI().toString();
        entityBuilder.addComponent(fallingBlockComponent);

        EntityRef entity = entityBuilder.build();
        delayManager.addDelayedAction(entity, PLACE_BLOCK_ACTION_ID, fallDurationInMs);
    }

    @ReceiveEvent
    public void onEntityCreationComplete(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (!event.getActionId().equals(COMPLETE_CREATE_ENTITY_ACTION_ID)) {
            return;
        }
        createEntityThatTriggersTheCompletionEvent(entityRef, transformation);
    }

    @ReceiveEvent
    public void onFallingBlockDelayElapsed(DelayedActionTriggeredEvent event, EntityRef entityRef,
                        FallingBlockComponent component, LocationComponent locationComponent) {
        if (event.getActionId().equals(PLACE_BLOCK_ACTION_ID)) {
            delayManager.addDelayedAction(entityRef, DESTROY_ENTITY_ACTION_ID, 1);
            Vector3f pos = locationComponent.getWorldPosition();
            Block block = blockManager.getBlock(component.blockUri);
            if (block == null) {
                logger.error("Block with url not found, block placement of fallen block skipped: " + Objects.toString(component.blockUri));
            }
            Vector3i roundedPos = new Vector3i(Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ()));
            logger.debug("Placing block at " + roundedPos);
            worldProvider.setBlock(roundedPos, block);
        }
        if (event.getActionId().equals(DESTROY_ENTITY_ACTION_ID)) {
            entityRef.destroy();
        }
    }

}
