// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.clipboard.ClipboardManager;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.InventoryComponent;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.engine.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerTargetChangedEvent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.RegionOutlineComponent;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.nui.Color;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.internal.ui.StructurePlacementFailureScreen;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Shows a preview/outline of the structure to spawn when the player hold a item with
 * {@link SpawnStructureActionComponent} and {@link SpawnBlockRegionsComponent}.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class StructureSpawnClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    public static final String STRUCTURE_PLACEMENT_FAILURE_OVERLAY = "StructureTemplates:StructurePlacementFailureScreen";
    @In
    private ClipboardManager clipboardManager;

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

    private List<EntityRef> regionOutlineEntities = new ArrayList<>();

    private Vector3i spawnPosition;

    private Side directionPlayerLooksAt;

    @In
    private Time time;


    @Override
    public void update(float delta) {
        updateOutlineIfNeeded();
    }

    private void updateOutlineIfNeeded() {
        LocationComponent locationComponent = locatPlayer.getCharacterEntity().getComponent(LocationComponent.class);
        if (locationComponent == null) {
            directionPlayerLooksAt = null;
            return;
        }

        Vector3f directionVector = locationComponent.getWorldDirection(new Vector3f());
        Side newDirection = Side.inHorizontalDirection(directionVector.x(), directionVector.z());
        if (directionPlayerLooksAt != newDirection) {
            directionPlayerLooksAt = newDirection;
            updateOutlineEntity();
        }
    }

    @ReceiveEvent
    public void onActivatedSpawnStructureActionComponent(OnActivatedComponent event, EntityRef entity,
                                                         SpawnStructureActionComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedSpawnStructureActionOrBlockRegions(OnChangedComponent event, EntityRef entity,
                                                            SpawnBlockRegionsComponent blockRegionComponent,
                                                            SpawnStructureActionComponent spawnStructureComponent) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onBeforeDeactivateSpawnStructureActionComponent(BeforeDeactivateComponent event, EntityRef entity,
                                                                SpawnStructureActionComponent component) {
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
        List<ColoredRegion> regionsToDraw = getRegionsToDraw();
        replaceRegionOutlineEntitiesWith(regionsToDraw);
    }

    void replaceRegionOutlineEntitiesWith(List<ColoredRegion> regionsToDraw) {
        for (EntityRef entity : regionOutlineEntities) {
            if (entity.exists()) {
                entity.destroy();
            }
        }

        for (ColoredRegion coloredRegion : regionsToDraw) {
            BlockRegion region = coloredRegion.region;
            Color color = coloredRegion.getColor();
            EntityRef entity = createOutlineEntity(region, color);
            regionOutlineEntities.add(entity);
        }
    }

    private EntityRef createOutlineEntity(BlockRegion region, Color color) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(false);
        RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
        regionOutlineComponent.corner1 = new Vector3i(region.getMin(new Vector3i()));
        regionOutlineComponent.corner2 = new Vector3i(region.getMax(new Vector3i()));
        regionOutlineComponent.color = color;
        entityBuilder.addComponent(regionOutlineComponent);
        return entityBuilder.build();
    }


    private List<ColoredRegion> getRegionsToDraw() {
        EntityRef characterEntity = locatPlayer.getCharacterEntity();
        SelectedInventorySlotComponent selectedSlotComponent = characterEntity.
                getComponent(SelectedInventorySlotComponent.class);
        if (selectedSlotComponent == null) {
            return Collections.emptyList();
        }
        InventoryComponent inventoryComponent = characterEntity.getComponent(InventoryComponent.class);
        if (inventoryComponent == null) {
            return Collections.emptyList();
        }
        inventoryComponent.itemSlots.get(selectedSlotComponent.slot);

        EntityRef item = inventoryManager.getItemInSlot(characterEntity, selectedSlotComponent.slot);
        SpawnBlockRegionsComponent spawnBlockRegionsComponent = item.getComponent(SpawnBlockRegionsComponent.class);
        if (spawnBlockRegionsComponent == null) {
            return Collections.emptyList();
        }

        SpawnStructureActionComponent spawnActionComponent = item.getComponent(SpawnStructureActionComponent.class);
        if (spawnActionComponent == null) {
            return Collections.emptyList();
        }
        if (spawnActionComponent.unconfirmSpawnErrorRegion != null) {
            return Arrays.asList(new ColoredRegion(spawnActionComponent.unconfirmSpawnErrorRegion, Color.RED));
        }

        if (spawnPosition == null) {
            return Collections.emptyList();
        }

        if (directionPlayerLooksAt == null) {
            return Collections.emptyList();
        }

        Side wantedFrontOfStructure = directionPlayerLooksAt.reverse();

        BlockRegionTransform regionTransform = StructureSpawnServerSystem.createBlockRegionTransformForCharacterTargeting(
                Side.FRONT, wantedFrontOfStructure, spawnPosition);

        List<ColoredRegion> regionsToDraw = new ArrayList<>();
        CheckSpawnConditionEvent checkSpawnEvent = new CheckSpawnConditionEvent(regionTransform);
        item.send(checkSpawnEvent);
        if (checkSpawnEvent.isPreventSpawn()) {
            BlockRegion problematicRegion = checkSpawnEvent.getSpawnPreventingRegion();
            if (problematicRegion != null) {
                regionsToDraw.add(new ColoredRegion(problematicRegion, Color.RED));
            }
        }

        for (SpawnBlockRegionsComponent.RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
            BlockRegion region = regionToFill.region;
            region = regionTransform.transformRegion(region);
            regionsToDraw.add(new ColoredRegion(region, Color.WHITE));
        }
        return regionsToDraw;
    }

    private static final class ColoredRegion {
        private BlockRegion region;
        private Color color;

        public ColoredRegion(BlockRegion region, Color color) {
            this.region = region;
            this.color = color;
        }

        public BlockRegion getRegion() {
            return region;
        }

        public Color getColor() {
            return color;
        }
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
