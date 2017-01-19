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

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.components.SpawnTemplateActionComponent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.internal.components.EditsCopyRegionComponent;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionRequest;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionResultEvent;
import org.terasology.structureTemplates.internal.events.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.structureTemplates.util.transform.HorizontalBlockRegionRotation;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamily;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Handles the activation of the copyBlockRegionTool item.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class StructureTemplateEditorServerSystem extends BaseComponentSystem {
    private static final Comparator<RegionToFill> REGION_BY_MIN_X_COMPARATOR = Comparator.comparing(r -> r.region.minX());
    private static final Comparator<RegionToFill> REGION_BY_MIN_Y_COMPARATOR = Comparator.comparing(r -> r.region.minY());
    private static final Comparator<RegionToFill> REGION_BY_MIN_Z_COMPARATOR = Comparator.comparing(r -> r.region.minZ());
    private static final Comparator<RegionToFill> REGION_BY_BLOCK_TYPE_COMPARATOR = Comparator.comparing(r -> r.blockType.getURI().toString());

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private EntitySystemLibrary entitySystemLibrary;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity,
                           StructureTemplateGeneratorComponent structureTemplateEditorComponent) {
        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }
        EntityRef owner = entity.getOwner();
        Vector3i position = blockComponent.getPosition();


        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();

        Region3i region = calculateDefaultRegion(frontDirectionOfStructure);

        boolean originPlaced = placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure, region);
        if (!originPlaced) {
            return;
        }
    }

    Region3i calculateDefaultRegion(Side frontDirectionOfStructure) {
        Region3i unrotatedRegion = Region3i.createBounded(new Vector3i(0, 1, 1), new Vector3i(0, 1, 1));


        HorizontalBlockRegionRotation rotation = HorizontalBlockRegionRotation.createRotationFromSideToSide(Side.FRONT,
                frontDirectionOfStructure);
        return rotation.transformRegion(unrotatedRegion);
    }


    private float sideToAngle(Side side) {
        switch (side) {
            case LEFT:
                return 0.5f * (float) Math.PI;
            case RIGHT:
                return -0.5f * (float) Math.PI;
            case BACK:
                return (float) Math.PI;
            default:
                return 0f;

        }
    }

    @ReceiveEvent
    public void updateCopyRegionOnBlockPlacement(PlaceBlocks placeBlocks, EntityRef world) {
        EntityRef player = placeBlocks.getInstigator().getOwner();
        EditsCopyRegionComponent editsCopyRegionComponent = player.getComponent(EditsCopyRegionComponent.class);
        if (editsCopyRegionComponent == null) {
            return;
        }
        EntityRef editorEnitity = editsCopyRegionComponent.structureTemplateEditor;
        StructureTemplateEditorComponent editorComponent = editorEnitity.getComponent(StructureTemplateEditorComponent.class);
        Region3i region3i = editorComponent.editRegion;

        final Map<Vector3i, Block> blocksMap = placeBlocks.getBlocks();
        for (Map.Entry<Vector3i, Block> blockEntry : blocksMap.entrySet()) {
            final Vector3i absolutePosition = blockEntry.getKey();
            final Vector3i relativePosition = new Vector3i(absolutePosition);
            relativePosition.sub(editorComponent.origin);
            if (!region3i.encompasses(relativePosition)) {
                region3i = region3i.expandToContain( relativePosition);
            }
        }
        if (!editorComponent.editRegion.equals(region3i)) {
            editorComponent.editRegion = region3i;
            editorEnitity.saveComponent(editorComponent);
        }
    }

    @ReceiveEvent
    public void onCreateStructureSpawnItemRequest(CreateStructureSpawnItemRequest event, EntityRef entity,
                                                  StructureTemplateEditorComponent structureTemplateEditorComponent,
                                                  StructureTemplateComponent structureTemplateComponent) {
        EntityBuilder entityBuilder = entityManager.newBuilder("StructureTemplates:structureSpawnItem");
        SpawnBlockRegionsComponent spawnBlockRegionsComponent = new SpawnBlockRegionsComponent();
        spawnBlockRegionsComponent.regionsToFill = createRegionsToFill(structureTemplateEditorComponent);
        entityBuilder.addOrSaveComponent(spawnBlockRegionsComponent);

        ComponentLibrary componentLibrary = entitySystemLibrary.getComponentLibrary();
        StructureTemplateComponent structureTemplateComponentCopy = componentLibrary.copy(structureTemplateComponent);
        entityBuilder.addOrSaveComponent(structureTemplateComponentCopy);
        EntityRef structureSpawnItem = entityBuilder.build();

        EntityRef character = event.getInstigator();
        // TODO check permission once PermissionManager is public API
        inventoryManager.giveItem(character, EntityRef.NULL, structureSpawnItem);
    }

    @ReceiveEvent
    public void onCopyBlockRegionRequest(CopyBlockRegionRequest event, EntityRef entity, StructureTemplateEditorComponent structureTemplateEditorComponent) {
        List<RegionToFill> regionsToFill = createRegionsToFill(structureTemplateEditorComponent);
        String textToSend = formatAsString(regionsToFill);

        CopyBlockRegionResultEvent resultEvent = new CopyBlockRegionResultEvent(textToSend);
        event.getInstigator().send(resultEvent);
    }


    @ReceiveEvent(components = {BlockItemComponent.class})
    public void onPlaced(OnBlockItemPlaced event, EntityRef itemEntity) {
        EntityRef placedBlockEntity = event.getPlacedBlock();
        StructureTemplateEditorComponent structureTemplateEditorComponent = placedBlockEntity.getComponent(StructureTemplateEditorComponent.class );
        if (structureTemplateEditorComponent == null) {
            return;
        }
        Vector3i origin = new Vector3i(event.getPosition());
        origin.subY(1); // block below marker
        structureTemplateEditorComponent.origin = origin;

        Side side = placedBlockEntity.getComponent(BlockComponent.class).getBlock().getDirection();
        structureTemplateEditorComponent.editRegion = calculateDefaultRegion(side);

        placedBlockEntity.saveComponent(structureTemplateEditorComponent);
    }

    private List<RegionToFill> createRegionsToFill(StructureTemplateEditorComponent structureTemplateEditorComponent) {
        Region3i absoluteRegion = structureTemplateEditorComponent.editRegion;
        absoluteRegion = absoluteRegion.move(structureTemplateEditorComponent.origin);
        List<RegionToFill> regionsToFill = new ArrayList<>();
        for (Vector3i absolutePosition : absoluteRegion) {
            Block block = worldProvider.getBlock(absolutePosition);
            RegionToFill regionToFill = new RegionToFill();
            Vector3i relativePosition = new Vector3i(absolutePosition);
            relativePosition.sub(structureTemplateEditorComponent.origin);
            Region3i region = Region3i.createBounded(relativePosition, relativePosition);
            regionToFill.region = region;
            regionToFill.blockType = block;
            regionsToFill.add(regionToFill);
        }
        mergeRegionsByX(regionsToFill);
        mergeRegionsByY(regionsToFill);
        mergeRegionsByZ(regionsToFill);
        regionsToFill.sort(REGION_BY_BLOCK_TYPE_COMPARATOR.thenComparing(REGION_BY_MIN_Z_COMPARATOR)
                .thenComparing(REGION_BY_MIN_X_COMPARATOR).thenComparing(REGION_BY_MIN_Y_COMPARATOR));
        return regionsToFill;
    }

    static Region3i regionWithMaxXSetTo(Region3i region, int newMaxX) {
        Vector3i max = new Vector3i(newMaxX, region.maxY(), region.maxZ());
        return Region3i.createBounded(region.min(), max);
    }

    static Region3i regionWithMaxYSetTo(Region3i region, int newMaxY) {
        Vector3i max = new Vector3i(region.maxX(), newMaxY, region.maxZ());
        return Region3i.createBounded(region.min(), max);
    }


    static Region3i regionWithMaxZSetTo(Region3i region, int newMaxZ) {
        Vector3i max = new Vector3i(region.maxX(), region.maxY(), newMaxZ);
        return Region3i.createBounded(region.min(), max);
    }


    static void mergeRegionsByX(List<RegionToFill> regions) {
        regions.sort(REGION_BY_MIN_Y_COMPARATOR.thenComparing(REGION_BY_MIN_Z_COMPARATOR).
                thenComparing(REGION_BY_MIN_X_COMPARATOR));
        List<RegionToFill> newList = new ArrayList<>();
        RegionToFill previous = null;
        for (RegionToFill r: regions) {
            boolean canMerge = previous != null && previous.region.maxX() == r.region.minX() -1
                    && r.region.minY() == previous.region.minY() && r.region.maxY() == previous.region.maxY()
                    && r.region.minZ() == previous.region.minZ() && r.region.maxZ() == previous.region.maxZ()
                    && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = regionWithMaxXSetTo(previous.region, r.region.maxX());
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsByY(List<RegionToFill> regions) {
        regions.sort(REGION_BY_MIN_X_COMPARATOR.thenComparing(REGION_BY_MIN_Z_COMPARATOR).
                thenComparing(REGION_BY_MIN_Y_COMPARATOR));
        List<RegionToFill> newList = new ArrayList<>();
        RegionToFill previous = null;
        for (RegionToFill r: regions) {
            boolean canMerge = previous != null && previous.region.maxY() == r.region.minY() -1
                    && r.region.minX() == previous.region.minX() && r.region.maxX() == previous.region.maxX()
                    && r.region.minZ() == previous.region.minZ() && r.region.maxZ() == previous.region.maxZ()
                    && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = regionWithMaxYSetTo(previous.region, r.region.maxY());
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsByZ(List<RegionToFill> regions) {
        regions.sort(REGION_BY_MIN_X_COMPARATOR.thenComparing(REGION_BY_MIN_Y_COMPARATOR).
                thenComparing(REGION_BY_MIN_Y_COMPARATOR));
        List<RegionToFill> newList = new ArrayList<>();
        RegionToFill previous = null;
        for (RegionToFill r: regions) {
            boolean canMerge = previous != null && previous.region.maxZ() == r.region.minZ() -1
                    && r.region.minX() == previous.region.minX() && r.region.maxX() == previous.region.maxX()
                    && r.region.minY() == previous.region.minY() && r.region.maxY() == previous.region.maxY()
                    && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = regionWithMaxZSetTo(previous.region, r.region.maxZ());
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static String formatAsString(List<RegionToFill> regionsToFill) {
        StringBuilder sb = new StringBuilder();
        for (RegionToFill regionToFill: regionsToFill) {
            sb.append("            { \"blockType\": \"");
            sb.append(regionToFill.blockType);
            sb.append("\", \"region\": { \"min\": [");
            sb.append(regionToFill.region.minX());
            sb.append(", ");
            sb.append(regionToFill.region.minY());
            sb.append(", ");
            sb.append(regionToFill.region.minZ());
            sb.append("], \"size\": [");
            sb.append(regionToFill.region.sizeX());
            sb.append(", ");
            sb.append(regionToFill.region.sizeY());
            sb.append(", ");
            sb.append(regionToFill.region.sizeZ());
            sb.append("]}},\n");
        }
        return sb.toString();
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
        BlockRegionTransform blockRegionTransform = StructureSpawnServerSystem.getBlockRegionTransformForStructurePlacement(
                event, structureTemplateComponent, blockComponent);
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

}
