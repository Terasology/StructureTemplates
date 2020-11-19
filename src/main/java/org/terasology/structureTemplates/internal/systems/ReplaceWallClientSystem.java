// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerTargetChangedEvent;
import org.terasology.math.Direction;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.nui.Color;
import org.terasology.registry.In;
import org.terasology.rendering.logic.RegionOutlineComponent;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.WallPreviewComponent;
import org.terasology.structureTemplates.internal.components.ReplaceWallItemComponent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.internal.ui.StructurePlacementFailureScreen;
import org.terasology.structureTemplates.util.RegionMergeUtil;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Shows a preview of the wall while the WallAdder items gets hold by the player.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ReplaceWallClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    public static final String STRUCTURE_PLACEMENT_FAILURE_OVERLAY = "StructureTemplates" +
            ":StructurePlacementFailureScreen";
    private static final int MAX_BLOCKS_PER_WALL = 100;

    @In
    private LocalPlayer locatPlayer;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private NUIManager nuiManager;

    @In
    private InputSystem inputSystem;

    @In
    private WorldProvider worldProvider;

    private List<EntityRef> regionOutlineEntities = new ArrayList<>();

    private Vector3i spawnPosition;

    private Side directionPlayerLooksAt;


    @Override
    public void update(float delta) {
        LocationComponent locationComponent = locatPlayer.getCharacterEntity().getComponent(LocationComponent.class);
        if (locationComponent == null) {
            directionPlayerLooksAt = null;
            return;
        }
        if (spawnPosition == null) {
            return;
        }
        Vector3f vectorToTarget = new Vector3f(spawnPosition);
        vectorToTarget.sub(locationComponent.getWorldPosition(new Vector3f()));

        Vector3f directionVector = vectorToTarget;
        Side newDirection = Side.inDirection(directionVector);
        if (directionPlayerLooksAt != newDirection) {
            directionPlayerLooksAt = newDirection;
            updateOutlineEntity();
        }

    }

    @ReceiveEvent
    public void onActivatedReplaceWallItemComponent(OnActivatedComponent event, EntityRef entity,
                                                    ReplaceWallItemComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedReplaceWallItemComponent(OnChangedComponent event, EntityRef entity,
                                                  ReplaceWallItemComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onBeforeDeactivateReplaceWallItemComponent(BeforeDeactivateComponent event, EntityRef entity,
                                                           ReplaceWallItemComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedSelectedInventorySlotComponent(OnChangedComponent event, EntityRef entity,
                                                        SelectedInventorySlotComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onPlayerTargetChangedEvent(PlayerTargetChangedEvent event, EntityRef entity) {
        EntityRef newTarget = event.getNewTarget();
        BlockComponent blockComponent = newTarget.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            spawnPosition = blockComponent.getPosition(new Vector3i());
        } else {
            spawnPosition = null;
        }
        updateOutlineEntity();
    }

    public void updateOutlineEntity() {
        EntityRef item = getSelectedItem();

        List<BlockRegion> regions = getRegionsOfWall(item);
        replaceRegionOutlineEntitiesWith(regions);

        WallPreviewComponent wallPreviewComponent = item.getComponent(WallPreviewComponent.class);
        if (wallPreviewComponent == null) {
            wallPreviewComponent = new WallPreviewComponent();
        }
        wallPreviewComponent.wallRegions = regions;
        item.addOrSaveComponent(wallPreviewComponent);
    }

    void replaceRegionOutlineEntitiesWith(Collection<BlockRegion> regionsToDraw) {
        for (EntityRef entity : regionOutlineEntities) {
            if (entity.exists()) {
                entity.destroy();
            }
        }
        regionOutlineEntities.clear();
        for (BlockRegion region : regionsToDraw) {
            EntityRef entity = createOutlineEntity(region, Color.GREEN);
            regionOutlineEntities.add(entity);
        }
    }

    // TODO reuse existing method
    private EntityRef createOutlineEntity(BlockRegion region, Color color) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(false);
        RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
        regionOutlineComponent.corner1 = JomlUtil.from(new Vector3i(region.getMin(new Vector3i())));
        regionOutlineComponent.corner2 = JomlUtil.from(new Vector3i(region.getMax(new Vector3i())));
        regionOutlineComponent.color = color;
        entityBuilder.addComponent(regionOutlineComponent);
        return entityBuilder.build();
    }

    private EntityRef getSelectedItem() {
        EntityRef characterEntity = locatPlayer.getCharacterEntity();
        SelectedInventorySlotComponent selectedSlotComponent = characterEntity.
                getComponent(SelectedInventorySlotComponent.class);
        if (selectedSlotComponent == null) {
            return EntityRef.NULL;
        }
        InventoryComponent inventoryComponent = characterEntity.getComponent(InventoryComponent.class);
        if (inventoryComponent == null) {
            return EntityRef.NULL;
        }
        inventoryComponent.itemSlots.get(selectedSlotComponent.slot);

        EntityRef item = inventoryManager.getItemInSlot(characterEntity, selectedSlotComponent.slot);
        return item;
    }


    private List<BlockRegion> getRegionsOfWall(EntityRef item) {
        Set<org.terasology.math.geom.Vector3i> positionsToAdd = getWallPositions(item);
        return RegionMergeUtil.mergePositionsIntoRegions(positionsToAdd);
    }

    private Vector3i getAbsoluteOffset(Side side, Direction relativeDirection) {
        return new Vector3i(getAbsoluteDirection(side, relativeDirection).toSide().direction());
    }

    private Direction getAbsoluteDirection(Side side, Direction relativeDirection) {
        switch (side) {
            case FRONT:
                return relativeDirection.toSide().yawClockwise(2).toDirection();
            case LEFT:
                return relativeDirection.toSide().yawClockwise(3).toDirection();
            case BACK:
                return relativeDirection.toSide().yawClockwise(0).toDirection();
            case RIGHT:
                return relativeDirection.toSide().yawClockwise(1).toDirection();
            case TOP:
                return relativeDirection.toSide().pitchClockwise(3).toDirection();
            case BOTTOM:
                return relativeDirection.toSide().pitchClockwise(1).toDirection();
        }
        throw new RuntimeException("Unsupported side " + side);
    }

    private Set<org.terasology.math.geom.Vector3i> getWallPositions(EntityRef item) {
        ReplaceWallItemComponent replaceWallItemComponent = item.getComponent(ReplaceWallItemComponent.class);
        if (replaceWallItemComponent == null) {
            return Collections.emptySet();
        }

        if (spawnPosition == null) {
            return Collections.emptySet();
        }

        if (directionPlayerLooksAt == null) {
            return Collections.emptySet();
        }

        Vector3i infrontDirection = getAbsoluteOffset(directionPlayerLooksAt, Direction.BACKWARD);
        Vector3i leftDirection = getAbsoluteOffset(directionPlayerLooksAt, Direction.LEFT);
        Vector3i rightDirection = getAbsoluteOffset(directionPlayerLooksAt, Direction.RIGHT);
        Vector3i upDirection = getAbsoluteOffset(directionPlayerLooksAt, Direction.UP);
        Vector3i downDirection = getAbsoluteOffset(directionPlayerLooksAt, Direction.DOWN);

        Set<org.terasology.math.geom.Vector3i> positionsToAdd = new HashSet<>();
        Set<Vector3i> positionsChecked = new HashSet<>();
        LinkedList<Vector3i> positionsToCheck = new LinkedList<>();
        positionsToCheck.add(spawnPosition);

        while (positionsToCheck.size() > 0 && positionsToAdd.size() < MAX_BLOCKS_PER_WALL) {
            Vector3i wallPosToCheck = positionsToCheck.pollFirst();
            if (positionsChecked.contains(wallPosToCheck)) {
                continue;
            }
            positionsChecked.add(wallPosToCheck);
            Vector3i infrontPosToCheck = new Vector3i(wallPosToCheck);
            infrontPosToCheck.add(infrontDirection);
            boolean infrontIsPenetratable = worldProvider.getBlock(infrontPosToCheck).isPenetrable();
            boolean isWall = !worldProvider.getBlock(wallPosToCheck).isPenetrable();
            boolean isValidWallPosition = infrontIsPenetratable && isWall;
            if (isValidWallPosition) {
                addPositionsBadedReplacementType(replaceWallItemComponent, leftDirection, rightDirection, upDirection,
                        downDirection, positionsToAdd, wallPosToCheck, infrontPosToCheck);
                positionsToCheck.add(vectorCopyWithOffset(wallPosToCheck, leftDirection));
                positionsToCheck.add(vectorCopyWithOffset(wallPosToCheck, rightDirection));
                positionsToCheck.add(vectorCopyWithOffset(wallPosToCheck, upDirection));
                positionsToCheck.add(vectorCopyWithOffset(wallPosToCheck, downDirection));
            }
        }
        return positionsToAdd;
    }


    private void addPositionsBadedReplacementType(ReplaceWallItemComponent replaceWallItemComponent,
                                                  Vector3i leftDirection, Vector3i rightDirection,
                                                  Vector3i upDirection, Vector3i downDirection,
                                                  Collection<org.terasology.math.geom.Vector3i> collectionToExtend,
                                                  Vector3i wallPos, Vector3i infrontWallPos) {
        switch (replaceWallItemComponent.replacementType) {
            case AIR_INFRONT_OF_WALL:
                collectionToExtend.add(JomlUtil.from(infrontWallPos));
                break;
            case WALL:
                collectionToExtend.add(JomlUtil.from(wallPos));
                break;
            case WALL_WITH_BORDER:
                collectionToExtend.add(JomlUtil.from(wallPos));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffset(wallPos, leftDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffset(wallPos, rightDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffset(wallPos, upDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffset(wallPos, downDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffsets(wallPos, leftDirection, upDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffsets(wallPos, leftDirection, downDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffsets(wallPos, rightDirection, upDirection)));
                collectionToExtend.add(JomlUtil.from(vectorCopyWithOffsets(wallPos, rightDirection, downDirection)));
                break;
        }
    }

    private Vector3i vectorCopyWithOffset(Vector3i vectorToCopy, Vector3i offset) {
        Vector3i vector = new Vector3i(vectorToCopy);
        vector.add(offset);
        return vector;
    }

    private Vector3i vectorCopyWithOffsets(Vector3i vectorToCopy, Vector3i offset1, Vector3i offset2) {
        Vector3i vector = new Vector3i(vectorToCopy);
        vector.add(offset1);
        vector.add(offset2);
        return vector;
    }


    @ReceiveEvent
    public void onStructureSpawnFailedEvent(StructureSpawnFailedEvent event, EntityRef entity,
                                            SpawnStructureActionComponent spawnActionComponent) {
        Prefab failedConditionPrefab = event.getFailedSpawnCondition();
        DisplayNameComponent displayNameComponent = failedConditionPrefab != null ? failedConditionPrefab
                .getComponent(DisplayNameComponent.class) : null;
        String spawnConditionName = displayNameComponent != null ? displayNameComponent.name : null;
        BlockRegion region = event.getSpawnPreventingRegion();
        String message;
        if (region != null) {
            if (spawnConditionName != null) {
                message = "Red region must contain: " + spawnConditionName;
            } else {
                message = "Red region prevented spawn";
            }
        } else {
            if (spawnConditionName != null) {
                message = "Placement condition not met: " + spawnConditionName;
            } else {
                message = "The failed placement condition has no description";
            }
        }
        StructurePlacementFailureScreen messagePopup = nuiManager.pushScreen(
                STRUCTURE_PLACEMENT_FAILURE_OVERLAY, StructurePlacementFailureScreen.class);
        messagePopup.setMessage(message);
    }
}
