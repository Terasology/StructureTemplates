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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.SpawnTemplateActionComponent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetStructureTemplateBlocksEvent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.events.StructureSpawnStartedEvent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.util.transform.BlockRegionMovement;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.structureTemplates.util.transform.BlockRegionTransformationList;
import org.terasology.structureTemplates.util.transform.HorizontalBlockRegionRotation;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamily;

import java.util.Map;

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

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private BlockManager blockManager;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onSpawnStructureEventWithHighestPriority(SpawnStructureEvent event, EntityRef entity) {
        entity.send(new StructureSpawnStartedEvent(event.getTransformation()));
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_NORMAL)
    public void onSpawnStructureEventWithBlocksPriority(SpawnStructureEvent event, EntityRef entity) {
        spawnBlocks(entity, event.getTransformation());
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onSpawnStructureEventWithLowestPriority(SpawnStructureEvent event, EntityRef entity) {
        entity.send(new StructureBlocksSpawnedEvent(event.getTransformation()));
    }


    private void spawnBlocks(EntityRef entity, BlockRegionTransform transformation) {
        long startTime = System.currentTimeMillis();
        GetStructureTemplateBlocksEvent getBlocksEvent =  new GetStructureTemplateBlocksEvent(transformation);
        entity.send(getBlocksEvent);
        Map<Vector3i, Block> blocksToPlace = getBlocksEvent.getBlocksToPlace();
        worldProvider.setBlocks(blocksToPlace);
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        if (delta > 20) {
            logger.warn("Structure of type {} took {} ms to spawn", entity.getParentPrefab().getName(), delta);
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_NORMAL)
    public void onSpawnTemplateEventWithBlocksPriority(SpawnTemplateEvent event, EntityRef entity) {
        spawnBlocks(entity, event.getTransformation());
    }

    @ReceiveEvent
    public void onGetStructureTemplateBlocks(GetStructureTemplateBlocksEvent event, EntityRef entity,
                                             SpawnBlockRegionsComponent spawnBlockRegionComponent) {
        BlockRegionTransform transformation = event.getTransformation();

        for (RegionToFill regionToFill : spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;

            Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            event.fillRegion(region, block);
        }
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

        BlockRegionTransform blockRegionTransform = getBlockRegionTransformForStructurePlacement(event, structureTemplateComponent, blockComponent);
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

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity,
                           SpawnTemplateActionComponent spawnActionComponent,
                           StructureTemplateComponent structureTemplateComponent) {
        EntityRef target = event.getTarget();
        BlockComponent blockComponent = target.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }

        Vector3i position = blockComponent.getPosition();
        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();


        Region3i unrotatedRegion = getPlacementBoundingsOfTemplate(entity);

        // TODO check for code sharing with StructureTemplateEditorServerSystem#onActivate
        HorizontalBlockRegionRotation rotation = HorizontalBlockRegionRotation.createRotationFromSideToSide(Side.FRONT,
                frontDirectionOfStructure);
        Region3i region = rotation.transformRegion(unrotatedRegion);

        boolean originPlaced = placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure, region);
        if (!originPlaced) {
            return;
        }
        BlockRegionTransform blockRegionTransform = getBlockRegionTransformForStructurePlacement(event, structureTemplateComponent, blockComponent);
        entity.send(new SpawnTemplateEvent(blockRegionTransform));

        // TODO check if consuming event and making item consumable works too e.g. event.consume();
        entity.destroy();
    }

    private Region3i getPlacementBoundingsOfTemplate(EntityRef entity) {
        Region3i unrotatedRegion = null;
        SpawnBlockRegionsComponent blockRegionsComponent = entity.getComponent(SpawnBlockRegionsComponent.class);
        if (blockRegionsComponent != null) {
            for (RegionToFill regionToFill : blockRegionsComponent.regionsToFill) {
                if (unrotatedRegion == null) {
                    unrotatedRegion = regionToFill.region;
                } else {
                    unrotatedRegion = unrotatedRegion.expandToContain(regionToFill.region.min());
                    unrotatedRegion = unrotatedRegion.expandToContain(regionToFill.region.max());
                }
            }
        }
        if (unrotatedRegion == null) {
            unrotatedRegion = Region3i.createBounded(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
        }
        return unrotatedRegion;
    }

    boolean placeOriginMarkerWithTemplateData(ActivateEvent event, Vector3i position, Side frontDirectionOfStructure, Region3i region) {
        boolean originPlaced = placeOriginMarkerBlockWithoutData(event, position, frontDirectionOfStructure);
        if (!originPlaced) {
            return false;
        }
        EntityRef originBlockEntity = blockEntityRegistry.getBlockEntityAt(position);
        addTemplateDataToBlockEntity(position, frontDirectionOfStructure, region, originBlockEntity);
        return true;
    }

    private void addTemplateDataToBlockEntity(Vector3i position, Side frontDirectionOfStructure, Region3i region, EntityRef originBlockEntity) {
        StructureTemplateEditorComponent editorComponent = originBlockEntity.getComponent(StructureTemplateEditorComponent.class);
        editorComponent.editRegion = region;
        editorComponent.origin.set(position);
        originBlockEntity.saveComponent(editorComponent);
        editorComponent.editRegion = region;
        editorComponent.origin.set(position);
        // TODO remove concept of front and store it somewhere in editor
        StructureTemplateComponent frontDirectionComponent = new StructureTemplateComponent();
        frontDirectionComponent.front = frontDirectionOfStructure;
        originBlockEntity.addOrSaveComponent(frontDirectionComponent);
    }

    private boolean placeOriginMarkerBlockWithoutData(ActivateEvent event, Vector3i position, Side frontDirectionOfStructure) {
        BlockFamily blockFamily = blockManager.getBlockFamily("StructureTemplates:StructureTemplateEditor");
        HorizontalBlockFamily horizontalBlockFamily = (HorizontalBlockFamily) blockFamily;
        Block block = horizontalBlockFamily.getBlockForSide(frontDirectionOfStructure);

        PlaceBlocks placeBlocks = new PlaceBlocks(position, block, event.getInstigator());
        worldProvider.getWorldEntity().send(placeBlocks);
        return !placeBlocks.isConsumed();
    }

    BlockRegionTransform getBlockRegionTransformForStructurePlacement(ActivateEvent event, StructureTemplateComponent structureTemplateComponent, BlockComponent blockComponent) {
        LocationComponent characterLocation = event.getInstigator().getComponent(LocationComponent.class);
        Vector3f directionVector = characterLocation.getWorldDirection();

        Side facedDirection = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side wantedFrontOfStructure = facedDirection.reverse();

        Side frontOfStructure = structureTemplateComponent.front;


        return createBlockRegionTransformForCharacterTargeting(frontOfStructure,
                wantedFrontOfStructure, blockComponent.getPosition());
    }

    public static BlockRegionTransform createBlockRegionTransformForCharacterTargeting(
            Side fromSide, Side toSide, Vector3i target) {
        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(fromSide, toSide));
        transformList.addTransformation(new BlockRegionMovement(target));
        return transformList;
    }

}
