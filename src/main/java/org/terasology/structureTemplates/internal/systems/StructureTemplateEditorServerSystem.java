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
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
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
import org.terasology.network.ClientComponent;
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
import org.terasology.structureTemplates.internal.events.MakeBoxShapedRequest;
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
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Vector3i position = blockComponent.getPosition();


        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();


        boolean originPlaced = placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure,
                Lists.newArrayList());
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

        Set<Vector3i> positionsInTemplate = new HashSet<>();
        for (Region3i region: editorComponent.absoluteRegionsWithTemplate) {
            for (Vector3i position: region) {
                positionsInTemplate.add(position);
            }
        }
        if (positionsInTemplate.containsAll(placeBlocks.getBlocks().keySet())) {
            return; // nothing to do
        }
        positionsInTemplate.addAll(placeBlocks.getBlocks().keySet());
        List<Region3i> newTemplateRegions = new ArrayList<>();
        for (Vector3i position: positionsInTemplate) {
            newTemplateRegions.add(Region3i.createFromMinMax(position, position));
        }
        mergeRegions(newTemplateRegions);
        editorComponent.absoluteRegionsWithTemplate = newTemplateRegions;
        editorEnitity.saveComponent(editorComponent);
    }

    @ReceiveEvent
    public void onCreateStructureSpawnItemRequest(CreateStructureSpawnItemRequest event, EntityRef entity,
                                                  StructureTemplateEditorComponent structureTemplateEditorComponent,
                                                  BlockComponent blockComponent) {
        EntityBuilder entityBuilder = entityManager.newBuilder("StructureTemplates:structureSpawnItem");
        SpawnBlockRegionsComponent spawnBlockRegionsComponent = new SpawnBlockRegionsComponent();
        spawnBlockRegionsComponent.regionsToFill = createRegionsToFill(structureTemplateEditorComponent, blockComponent);
        entityBuilder.addOrSaveComponent(spawnBlockRegionsComponent);

        StructureTemplateComponent structureTemplateComponent = new StructureTemplateComponent();
        entityBuilder.addOrSaveComponent(structureTemplateComponent);
        EntityRef structureSpawnItem = entityBuilder.build();

        EntityRef character = event.getInstigator().getOwner().getComponent(ClientComponent.class).character;
        // TODO check permission once PermissionManager is public API
        inventoryManager.giveItem(character, EntityRef.NULL, structureSpawnItem);
    }

    @ReceiveEvent
    public void onCopyBlockRegionRequest(CopyBlockRegionRequest event, EntityRef entity,
                                         StructureTemplateEditorComponent structureTemplateEditorComponent,
                                         BlockComponent blockComponent) {
        List<RegionToFill> regionsToFill = createRegionsToFill(structureTemplateEditorComponent, blockComponent);
        String textToSend = formatAsString(regionsToFill);

        CopyBlockRegionResultEvent resultEvent = new CopyBlockRegionResultEvent(textToSend);
        event.getInstigator().send(resultEvent);
    }


    @ReceiveEvent
    public void onMakeBoxShapedRequest(MakeBoxShapedRequest event, EntityRef entity,
                                       StructureTemplateEditorComponent structureTemplateEditorComponent) {
        structureTemplateEditorComponent.absoluteRegionsWithTemplate = new ArrayList<>(Arrays.asList(event.getRegion()));
        entity.saveComponent(structureTemplateEditorComponent);
    }


    @ReceiveEvent(components = {BlockItemComponent.class})
    public void onBlockItemPlaced(OnBlockItemPlaced event, EntityRef itemEntity,
                         StructureTemplateEditorComponent componentOfItem) {
        EntityRef placedBlockEntity = event.getPlacedBlock();

        StructureTemplateEditorComponent componentOfBlock = placedBlockEntity.getComponent(StructureTemplateEditorComponent.class );
        if (componentOfBlock == null) {
            componentOfBlock = new StructureTemplateEditorComponent();
        }
        Vector3i origin = new Vector3i(event.getPosition());
        componentOfBlock.absoluteRegionsWithTemplate = new ArrayList<>(componentOfItem.absoluteRegionsWithTemplate);
        placedBlockEntity.saveComponent(componentOfBlock);
    }

    @ReceiveEvent(components = {})
    public void onBlockToItem(OnBlockToItem event, EntityRef blockEntity,
                                   StructureTemplateEditorComponent componentOfBlock) {
        EntityRef item = event.getItem();
        StructureTemplateEditorComponent componentOfItem = item.getComponent(StructureTemplateEditorComponent.class);
        if (componentOfItem == null) {
            componentOfItem = new StructureTemplateEditorComponent();
        }
        componentOfItem.absoluteRegionsWithTemplate = new ArrayList<>(componentOfBlock.absoluteRegionsWithTemplate);
        item.saveComponent(componentOfItem);
    }

    private List<RegionToFill> createRegionsToFill(StructureTemplateEditorComponent structureTemplateEditorComponent, BlockComponent blockComponent) {
        List<Region3i> absoluteRegions = structureTemplateEditorComponent.absoluteRegionsWithTemplate;
        BlockRegionTransform transformToRelative = createAbsoluteToRelativeTransform(blockComponent);

        List<RegionToFill> regionsToFill = new ArrayList<>();
        for (Region3i absoluteRegion: absoluteRegions) {
            for (Vector3i absolutePosition : absoluteRegion) {
                Block block = worldProvider.getBlock(absolutePosition);
                RegionToFill regionToFill = new RegionToFill();
                Vector3i relativePosition = transformToRelative.transformVector3i(absolutePosition);
                Region3i region = Region3i.createBounded(relativePosition, relativePosition);
                regionToFill.region = region;
                regionToFill.blockType = block;
                regionsToFill.add(regionToFill);
            }
        }
        mergeRegionsToFill(regionsToFill);
        regionsToFill.sort(REGION_BY_BLOCK_TYPE_COMPARATOR.thenComparing(REGION_BY_MIN_Z_COMPARATOR)
                .thenComparing(REGION_BY_MIN_X_COMPARATOR).thenComparing(REGION_BY_MIN_Y_COMPARATOR));
        return regionsToFill;
    }



    // TODO move 2 methods to utility class
    public static BlockRegionTransform createAbsoluteToRelativeTransform(BlockComponent blockComponent) {
        Side front = blockComponent.getBlock().getDirection();
        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        Vector3i minusOrigin = new Vector3i(0, 0, 0);
        minusOrigin.sub(blockComponent.getPosition());
        transformList.addTransformation(new BlockRegionMovement(minusOrigin));
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(front, Side.FRONT));
        return transformList;
    }
    public static BlockRegionTransform createRelativeToAbsoluteTransform(BlockComponent blockComponent) {
        Side front = blockComponent.getBlock().getDirection();
        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(Side.FRONT, front));
        transformList.addTransformation(new BlockRegionMovement(new Vector3i(blockComponent.getPosition())));
        return transformList;
    }

    private enum RegionDimension {
        X {
            public int getMin(Region3i r) {
                return r.minX();
            }

            public int getMax(Region3i r) {
                return r.maxX();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(newMax, r.maxY(), r.maxZ());
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minX());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minX());
            }
        },
        Y {
            public int getMin(Region3i r) {
                return r.minY();
            }

            public int getMax(Region3i r) {
                return r.maxY();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(r.maxX(), newMax, r.maxZ());
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minY());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minY());
            }
        },
        Z {
            public int getMin(Region3i r) {
                return r.minZ();
            }

            public int getMax(Region3i r) {
                return r.maxZ();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(r.maxX(), r.maxY(), newMax);
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minZ());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minZ());
            }
        };
        public abstract int getMin(Region3i r);
        public abstract int getMax(Region3i r);
        public abstract Region3i regionCopyWithMaxSetTo(Region3i r, int newMax);
        public Region3i regionCopyWithMaxOfSecond(Region3i regionToCopy, Region3i regionWithMax) {
            int newMax = getMax(regionWithMax);
            return regionCopyWithMaxSetTo(regionToCopy, newMax);
        }
        public abstract Comparator<RegionToFill> regionToFillComparator();
        public abstract Comparator<Region3i> regionComparator();
    }

    static void mergeRegionsToFillByDimension(List<RegionToFill> regions, RegionDimension dimensionToMerge,
                                              RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionToFillComparator().thenComparing(thirdDimension.regionToFillComparator()).
                thenComparing(dimensionToMerge.regionToFillComparator()));
        List<RegionToFill> newList = new ArrayList<>();
        RegionToFill previous = null;
        for (RegionToFill r: regions) {
            boolean canMerge = previous != null && dimensionToMerge.getMax(previous.region) == dimensionToMerge.getMin(r.region) -1
                    && secondaryDimension.getMin(r.region) == secondaryDimension.getMin(previous.region)
                    && secondaryDimension.getMax(r.region) == secondaryDimension.getMax(previous.region)
                    && thirdDimension.getMin(r.region) == thirdDimension.getMin(previous.region)
                    && thirdDimension.getMax(r.region) == thirdDimension.getMax(previous.region)
                    && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = dimensionToMerge.regionCopyWithMaxSetTo(previous.region, dimensionToMerge.getMax(r.region));
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsByDimension(List<Region3i> regions, RegionDimension dimensionToMerge,
                                        RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionComparator().thenComparing(thirdDimension.regionComparator()).
                thenComparing(dimensionToMerge.regionComparator()));
        List<Region3i> newList = new ArrayList<>();
        Region3i previous = null;
        for (Region3i r: regions) {
            boolean canMerge = previous != null && dimensionToMerge.getMax(previous) == dimensionToMerge.getMin(r) -1
                    && secondaryDimension.getMin(r) == secondaryDimension.getMin(previous)
                    && secondaryDimension.getMax(r) == secondaryDimension.getMax(previous)
                    && thirdDimension.getMin(r) == thirdDimension.getMin(previous)
                    && thirdDimension.getMax(r) == thirdDimension.getMax(previous);
            if (canMerge) {
                // Remove previous:
                newList.remove(newList.size()-1);
                previous = dimensionToMerge.regionCopyWithMaxOfSecond(previous, r);
                newList.add(previous);
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    private void mergeRegionsToFill(List<RegionToFill> regionsToFill) {
        mergeRegionsToFillByX(regionsToFill);
        mergeRegionsToFillByY(regionsToFill);
        mergeRegionsToFillByZ(regionsToFill);
    }

    static void mergeRegionsToFillByX(List<RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.X, RegionDimension.Y, RegionDimension.Z);
    }

    static void mergeRegionsToFillByY(List<RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.Y, RegionDimension.Z, RegionDimension.X);
    }

    static void mergeRegionsToFillByZ(List<RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.Z, RegionDimension.X, RegionDimension.Y);
    }


    private void mergeRegions(List<Region3i> regions) {
        mergeRegionsByX(regions);
        mergeRegionsByY(regions);
        mergeRegionsByZ(regions);
    }

    static void mergeRegionsByX(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.X, RegionDimension.Y, RegionDimension.Z);
    }

    static void mergeRegionsByY(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Y, RegionDimension.Z, RegionDimension.X);
    }

    static void mergeRegionsByZ(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Z, RegionDimension.X, RegionDimension.Y);
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
                           SpawnTemplateActionComponent spawnActionComponent) {
        EntityRef target = event.getTarget();
        BlockComponent blockComponent = target.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }

        Vector3i position = blockComponent.getPosition();
        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();


        List<Region3i> absoluteRegions = getAbsolutePlacementRegionsOfTemplate(entity, position, frontDirectionOfStructure);

        boolean originPlaced = placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure, absoluteRegions);
        if (!originPlaced) {
            return;
        }
        BlockRegionTransform blockRegionTransform = StructureSpawnServerSystem.getBlockRegionTransformForStructurePlacement(
                event, blockComponent);
        entity.send(new SpawnTemplateEvent(blockRegionTransform));

        // TODO check if consuming event and making item consumable works too e.g. event.consume();
        entity.destroy();
    }

    List<Region3i> getAbsolutePlacementRegionsOfTemplate(EntityRef entity, Vector3i position, Side frontDirectionOfStructure) {
        List<Region3i> relativeRegions = getPlacementRegionsOfTemplate(entity);

        // TODO reuse createRelativeToAbsoluteTransform
        HorizontalBlockRegionRotation rotation = HorizontalBlockRegionRotation.createRotationFromSideToSide(Side.FRONT,
                frontDirectionOfStructure);
        List<Region3i> absoluteRegions = new ArrayList<>();
        for (Region3i relativeRegion: relativeRegions) {
            Region3i absoluteRegion = rotation.transformRegion(relativeRegion);
            absoluteRegion = absoluteRegion.move(position);
            absoluteRegions.add(absoluteRegion);
        }
        return absoluteRegions;
    }

    private List<Region3i> getPlacementRegionsOfTemplate(EntityRef entity) {
        List<Region3i> relativeRegions = Lists.newArrayList();
        SpawnBlockRegionsComponent blockRegionsComponent = entity.getComponent(SpawnBlockRegionsComponent.class);
        if (blockRegionsComponent != null) {
            for (RegionToFill regionToFill : blockRegionsComponent.regionsToFill) {
                relativeRegions.add(regionToFill.region);
            }
        }
        return relativeRegions;
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

    boolean placeOriginMarkerWithTemplateData(ActivateEvent event, Vector3i position, Side frontDirectionOfStructure, List<Region3i> regions) {
        boolean originPlaced = placeOriginMarkerBlockWithoutData(event, position, frontDirectionOfStructure);
        if (!originPlaced) {
            return false;
        }
        EntityRef originBlockEntity = blockEntityRegistry.getBlockEntityAt(position);
        addTemplateDataToBlockEntity(position, regions, originBlockEntity);
        return true;
    }

    private void addTemplateDataToBlockEntity(Vector3i position, List<Region3i> regions, EntityRef originBlockEntity) {
        StructureTemplateEditorComponent editorComponent = originBlockEntity.getComponent(StructureTemplateEditorComponent.class);
        editorComponent.absoluteRegionsWithTemplate = new ArrayList<>(regions);
        originBlockEntity.saveComponent(editorComponent);
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
