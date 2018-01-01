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

import org.terasology.engine.Time;
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
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerTargetChangedEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.logic.RegionOutlineComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.internal.events.StructureSpawnFailedEvent;
import org.terasology.structureTemplates.internal.ui.StructurePlacementFailureScreen;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.block.BlockComponent;

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

        Vector3f directionVector = locationComponent.getWorldDirection();
        Side newDirection = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
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
            spawnPosition = blockComponent.getPosition();
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
            Region3i region = coloredRegion.region;
            Color color = coloredRegion.getColor();
            EntityRef entity = createOutlineEntity(region, color);
            regionOutlineEntities.add(entity);
        }
    }

    private EntityRef createOutlineEntity(Region3i region, Color color) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(false);
        RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
        regionOutlineComponent.corner1 = new Vector3i(region.min());
        regionOutlineComponent.corner2 = new Vector3i(region.max());
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
            Region3i problematicRegion = checkSpawnEvent.getSpawnPreventingRegion();
            if (problematicRegion != null) {
                regionsToDraw.add(new ColoredRegion(problematicRegion, Color.RED));
            }
        }

        for (SpawnBlockRegionsComponent.RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
            Region3i region = regionToFill.region;
            region = regionTransform.transformRegion(region);
            regionsToDraw.add(new ColoredRegion(region, Color.WHITE));
        }
        return regionsToDraw;
    }

    private static final class ColoredRegion {
        private Region3i region;
        private Color color;

        public ColoredRegion(Region3i region, Color color) {
            this.region = region;
            this.color = color;
        }

        public Region3i getRegion() {
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
        Region3i region = event.getSpawnPreventingRegion();
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