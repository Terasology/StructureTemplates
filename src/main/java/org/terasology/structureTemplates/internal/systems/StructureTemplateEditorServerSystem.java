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
import com.google.common.collect.Sets;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.HorizontalFamily;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;
import org.terasology.engine.world.block.items.OnBlockToItem;
import org.terasology.structureTemplates.components.BlockPlaceholderComponent;
import org.terasology.structureTemplates.components.FallingBlocksPlacementAlgorithmComponent;
import org.terasology.structureTemplates.components.NoConstructionAnimationComponent;
import org.terasology.structureTemplates.components.ScheduleStructurePlacementComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.components.SpawnTemplateActionComponent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.BuildStructureTemplateEntityEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.internal.components.EditTemplateRegionProcessComponent;
import org.terasology.structureTemplates.internal.components.EditingUserComponent;
import org.terasology.structureTemplates.internal.components.StructurePlaceholderComponent;
import org.terasology.structureTemplates.internal.components.StructureTemplateGeneratorComponent;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;
import org.terasology.structureTemplates.internal.events.BuildStructureTemplateStringEvent;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionResultEvent;
import org.terasology.structureTemplates.internal.events.CreateEditTemplateRegionProcessRequest;
import org.terasology.structureTemplates.internal.events.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.internal.events.CreateStructureTemplateItemRequest;
import org.terasology.structureTemplates.internal.events.MakeBoxShapedRequest;
import org.terasology.structureTemplates.internal.events.RequestStructurePlaceholderPrefabSelection;
import org.terasology.structureTemplates.internal.events.RequestStructureTemplatePropertiesChange;
import org.terasology.structureTemplates.internal.events.StopEditingProcessRequest;
import org.terasology.structureTemplates.internal.events.StructureTemplateStringRequest;
import org.terasology.structureTemplates.util.AnimationType;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.structureTemplates.util.ListUtil;
import org.terasology.structureTemplates.util.RegionMergeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the activation of the copyBlockRegionTool item.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class StructureTemplateEditorServerSystem extends BaseComponentSystem {
    private static final Comparator<RegionToFill> REGION_BY_MIN_X_COMPARATOR = Comparator.comparing(r -> r.region.minX());
    private static final Comparator<RegionToFill> REGION_BY_MIN_Y_COMPARATOR = Comparator.comparing(r -> r.region.minY());
    private static final Comparator<RegionToFill> REGION_BY_MIN_Z_COMPARATOR = Comparator.comparing(r -> r.region.minZ());
    private static final Comparator<RegionToFill> REGION_BY_BLOCK_TYPE_COMPARATOR = Comparator.comparing(r -> r.blockType.getURI().toString());
    private static final Logger LOGGER = LoggerFactory.getLogger(StructureTemplateEditorServerSystem.class);

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

    @In
    private ComponentLibrary componentLibrary;

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity,
                           StructureTemplateGeneratorComponent structureTemplateEditorComponent) {
        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            return;
        }
        Vector3i position = blockComponent.getPosition(new Vector3i());


        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.x(), directionVector.z());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();

        StructureTemplateComponent component = new StructureTemplateComponent();

        boolean originPlaced = placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure,
            Lists.newArrayList(), component);
        if (!originPlaced) {
            return;
        }
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
        EditingUserComponent editingUserComponent = player.getComponent(EditingUserComponent.class);
        if (editingUserComponent == null) {
            return;
        }

        EntityRef editProcessEntity = editingUserComponent.editProcessEntity;
        EditTemplateRegionProcessComponent editTemplateRegionProcessComponent = editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
        if (editTemplateRegionProcessComponent == null) {
            return;
        }
        EntityRef editorEnitity = editTemplateRegionProcessComponent.structureTemplateEditor;
        StructureTemplateOriginComponent editorComponent = editorEnitity.getComponent(StructureTemplateOriginComponent.class);
        if (editorComponent == null) {
            return; // can happen if entity got destroyed
        }
        if (editTemplateRegionProcessComponent.recordBlockAddition) {
            addBlockPositionsToTemplate(placeBlocks.getBlocks().keySet(), editorEnitity, editorComponent);
        }
        if (editTemplateRegionProcessComponent.recordBlockRemoval && !editTemplateRegionProcessComponent.recordBlockAddition) {
            removeBlockPositionsFromTemplate(placeBlocks.getBlocks().keySet(), editorEnitity, editorComponent);
        }
    }

    @ReceiveEvent
    public void onDestroyed(DoDestroyEvent event, EntityRef entity, BlockComponent blockComponent) {
        EntityRef instigator = event.getInstigator();
        if (instigator == null) {
            return;
        }
        EntityRef player = instigator.getOwner();
        EditingUserComponent editingUserComponent = player.getComponent(EditingUserComponent.class);
        if (editingUserComponent == null) {
            return;
        }

        EntityRef editProcessEntity = editingUserComponent.editProcessEntity;
        EditTemplateRegionProcessComponent editTemplateRegionProcessComponent = editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
        if (editTemplateRegionProcessComponent == null) {
            return;
        }
        EntityRef editorEnitity = editTemplateRegionProcessComponent.structureTemplateEditor;
        StructureTemplateOriginComponent editorComponent = editorEnitity.getComponent(StructureTemplateOriginComponent.class);
        if (editorComponent == null) {
            return; // can happen if entity got destroyed
        }
        Set<Vector3ic> positionSet = Collections.singleton(blockComponent.getPosition(new Vector3i()));
        if (editTemplateRegionProcessComponent.recordBlockAddition && !editTemplateRegionProcessComponent.recordBlockRemoval) {
            removeBlockPositionsFromTemplate(positionSet, editorEnitity, editorComponent);
        }
        if (editTemplateRegionProcessComponent.recordBlockRemoval) {
            addBlockPositionsToTemplate(positionSet, editorEnitity, editorComponent);
        }
    }

    private void addBlockPositionsToTemplate(Set<Vector3ic> positions, EntityRef templateEntity, StructureTemplateOriginComponent templateComponent) {
        List<BlockRegion> originalRegions = templateComponent.absoluteTemplateRegions;
        Set<Vector3ic> positionsInTemplate = Sets.newHashSet();
        positionsInTemplate.addAll(RegionMergeUtil.positionsOfRegions(originalRegions));
        if (positionsInTemplate.containsAll(positions)) {
            // nothing to do
        } else {
            positionsInTemplate.addAll(positions);
            templateComponent.absoluteTemplateRegions = RegionMergeUtil.mergePositionsIntoRegions(positionsInTemplate);
            templateEntity.saveComponent(templateComponent);
        }
    }

    private void removeBlockPositionsFromTemplate(Set<Vector3ic> positions, EntityRef templateEntity, StructureTemplateOriginComponent templateComponent) {
        List<BlockRegion> originalRegions = templateComponent.absoluteTemplateRegions;
        Set<Vector3ic> positionsInTemplate = Sets.newHashSet();
        positionsInTemplate.addAll(RegionMergeUtil.positionsOfRegions(originalRegions));
        if (!positionsInTemplate.containsAll(positions)) {
            // nothing to do
        } else {
            positionsInTemplate.removeAll(positions);
            List<BlockRegion> newTemplateRegions = RegionMergeUtil.mergePositionsIntoRegions(positionsInTemplate);
            templateComponent.absoluteTemplateRegions = newTemplateRegions;
            templateEntity.saveComponent(templateComponent);
        }
    }

    @ReceiveEvent
    public void onRequestStructurePlaceholderPrefabSelection(RequestStructurePlaceholderPrefabSelection event, EntityRef characterEntity,
                                                             CharacterComponent characterComponent) {
        EntityRef interactionTarget = characterComponent.authorizedInteractionTarget;
        StructurePlaceholderComponent structurePlaceholderComponent = interactionTarget.getComponent(StructurePlaceholderComponent.class);
        if (structurePlaceholderComponent == null) {
            LOGGER.error("Ignored RequestStructurePlaceholderPrefabSelection event since there was no interaction with a structure placeholder");
            return;
        }

        structurePlaceholderComponent.selectedPrefab = event.getPrefab();
        interactionTarget.saveComponent(structurePlaceholderComponent);
    }

    @ReceiveEvent
    public void onRequestStructureTemplatePropertiesChange(RequestStructureTemplatePropertiesChange event,
                                                           EntityRef characterEntity, CharacterComponent characterComponent) {
        EntityRef interactionTarget = characterComponent.authorizedInteractionTarget;
        StructureTemplateComponent structureTemplateComponent = interactionTarget.getComponent(StructureTemplateComponent.class);
        if (structureTemplateComponent == null) {
            LOGGER.error("Ignored RequestStructureTemplatePropertiesChange event since there was no interaction with structure template ");
            return;
        }
        if (!interactionTarget.hasComponent(StructureTemplateOriginComponent.class)) {
            LOGGER.error("Ignored RequestStructureTemplatePropertiesChange event since there was no interaction with structure template origin");
            return;
        }

        structureTemplateComponent.type = event.getPrefab();
        structureTemplateComponent.spawnChance = event.getSpawnChance();
        structureTemplateComponent.animationType = event.getAnimationType();
        interactionTarget.saveComponent(structureTemplateComponent);
    }

    @ReceiveEvent
    public void onCreateEditTemplateRegionProcessRequest(CreateEditTemplateRegionProcessRequest event,
                                                         EntityRef characterEntity, CharacterComponent characterComponent) {
        EntityRef interactionTarget = characterComponent.authorizedInteractionTarget;
        StructureTemplateComponent structureTemplateComponent = interactionTarget.getComponent(StructureTemplateComponent.class);
        if (structureTemplateComponent == null) {
            LOGGER.error("Ignored CreateEditTemplateRegionProcessRequest event since there was no interaction with structure template ");
            return;
        }
        if (!interactionTarget.hasComponent(StructureTemplateOriginComponent.class)) {
            LOGGER.error("Ignored CreateEditTemplateRegionProcessRequest event since there was no interaction with structure template origin");
            return;
        }
        EntityRef client = characterEntity.getOwner();
        startEditingProcess(event, interactionTarget, client);
    }

    private void startEditingProcess(CreateEditTemplateRegionProcessRequest event, EntityRef interactionTarget, EntityRef client) {
        EntityBuilder editProcessBuilder = entityManager.newBuilder();
        editProcessBuilder.setPersistent(false);
        editProcessBuilder.addComponent(new NetworkComponent());
        editProcessBuilder.setOwner(client);
        EditTemplateRegionProcessComponent editTemplateRegionProcessComponent = new EditTemplateRegionProcessComponent();
        editTemplateRegionProcessComponent.structureTemplateEditor = interactionTarget;
        editTemplateRegionProcessComponent.recordBlockAddition = event.isRecordBlockAddition();
        editTemplateRegionProcessComponent.recordBlockRemoval = event.isRecordBlockRemoval();
        editProcessBuilder.addComponent(editTemplateRegionProcessComponent);
        EditingUserComponent editingUserComponent = client.getComponent(EditingUserComponent.class);
        if (editingUserComponent == null) {
            editingUserComponent = new EditingUserComponent();
        } else {
            editingUserComponent.editProcessEntity.destroy();
        }
        editingUserComponent.editProcessEntity = editProcessBuilder.build();
        client.addOrSaveComponent(editingUserComponent);
    }

    @ReceiveEvent
    public void onStopEditingProcessRequest(StopEditingProcessRequest event, EntityRef client) {
        EditingUserComponent editingUserComponent = client.getComponent(EditingUserComponent.class);
        if (editingUserComponent != null) {
            EditTemplateRegionProcessComponent editProcessComponent = editingUserComponent.editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
            editingUserComponent.editProcessEntity.destroy();
            client.removeComponent(EditingUserComponent.class);
        }
    }

    @ReceiveEvent
    public void onCreateStructureSpawnItemRequest(CreateStructureSpawnItemRequest event, EntityRef characterEntity,
                                                  CharacterComponent characterComponent) {
        EntityRef structureTemplateOriginEntity = characterComponent.authorizedInteractionTarget;
        StructureTemplateOriginComponent structureTemplateOriginComponent = structureTemplateOriginEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            LOGGER.error("Ignored CreateStructureSpawnItemRequest event since there was no interaction with a structure template origin block");
            return;
        }

        BlockComponent blockComponent = structureTemplateOriginEntity.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            LOGGER.error("Structure template origin was not a block, ignoring event");
            return;
        }

        EntityBuilder entityBuilder = entityManager.newBuilder("StructureTemplates:structureSpawnItem");
        addComponentsToTemplate(structureTemplateOriginEntity, structureTemplateOriginComponent, blockComponent, entityBuilder);
        // TODO allow the player to select construction animation
        EntityRef structureSpawnItem = entityBuilder.build();

        // TODO check permission once PermissionManager is public API
        inventoryManager.giveItem(characterEntity, EntityRef.NULL, structureSpawnItem);
    }


    @ReceiveEvent
    public void onCreateStructureTemplateItemRequest(CreateStructureTemplateItemRequest event, EntityRef characterEntity,
                                                     CharacterComponent characterComponent) {
        EntityRef structureTemplateOriginEntity = characterComponent.authorizedInteractionTarget;
        StructureTemplateOriginComponent structureTemplateOriginComponent = structureTemplateOriginEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            LOGGER.error("Ignored CreateStructureTemplateItemRequest event since there was no interaction with a structure template origin block");
            return;
        }

        BlockComponent blockComponent = structureTemplateOriginEntity.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            LOGGER.error("Structure template origin was not a block, ignoring event");
            return;
        }

        EntityBuilder entityBuilder = entityManager.newBuilder("StructureTemplates:structureTemplateItem");
        addComponentsToTemplate(structureTemplateOriginEntity, structureTemplateOriginComponent, blockComponent, entityBuilder);
        EntityRef structureSpawnItem = entityBuilder.build();

        // TODO check permission once PermissionManager is public API
        inventoryManager.giveItem(characterEntity, EntityRef.NULL, structureSpawnItem);
    }

    private void addComponentsToTemplate(EntityRef editorEntity,
                                         StructureTemplateOriginComponent structureTemplateOriginComponent,
                                         BlockComponent blockComponent,
                                         MutableComponentContainer templateEntity) {
        BlockRegionTransform transformToRelative = createAbsoluteToRelativeTransform(blockComponent);

        Map<Block, Set<Vector3i>> blockToAbsPositionsMap = createBlockToAbsolutePositionsMap(
            structureTemplateOriginComponent);


        BuildStructureTemplateEntityEvent createTemplateEvent = new BuildStructureTemplateEntityEvent(templateEntity,
            transformToRelative, blockToAbsPositionsMap);
        editorEntity.send(createTemplateEvent);
    }

    @ReceiveEvent
    public void onBuildStructureTemplate(BuildStructureTemplateEntityEvent event, EntityRef entity,
                                         StructureTemplateComponent componentOfEditor) {
        MutableComponentContainer templateEntity = event.getTemplateEntity();
        templateEntity.addOrSaveComponent(componentLibrary.copy(componentOfEditor));
        if (componentOfEditor.animationType == AnimationType.FallingBlock) {
            templateEntity.addOrSaveComponent(new FallingBlocksPlacementAlgorithmComponent());
        } else if (componentOfEditor.animationType == AnimationType.NoAnimation) {
            templateEntity.addOrSaveComponent(new NoConstructionAnimationComponent());
        }
    }

    @ReceiveEvent
    public void onBuildTemplateWithBlockRegions(BuildStructureTemplateEntityEvent event, EntityRef entity,
                                                StructureTemplateOriginComponent structureTemplateOriginComponent) {
        BlockRegionTransform transformToRelative = event.getTransformToRelative();
        SpawnBlockRegionsComponent spawnBlockRegionsComponent = new SpawnBlockRegionsComponent();
        spawnBlockRegionsComponent.regionsToFill = createRegionsToFill(structureTemplateOriginComponent,
            transformToRelative);
        MutableComponentContainer templateEntity = event.getTemplateEntity();
        templateEntity.addOrSaveComponent(spawnBlockRegionsComponent);
    }

    @ReceiveEvent
    public void onBuildTemplateWithScheduledStructurePlacment(BuildStructureTemplateEntityEvent event, EntityRef entity) {
        BlockRegionTransform transformToRelative = event.getTransformToRelative();
        BlockFamily blockFamily = blockManager.getBlockFamily("StructureTemplates:StructurePlaceholder");

        List<ScheduleStructurePlacementComponent.PlacementToSchedule> placementToSchedules = new ArrayList<>();
        for (Vector3i position : event.findAbsolutePositionsOf(blockFamily)) {
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(position);
            StructurePlaceholderComponent structurePlaceholderComponent = blockEntity.getComponent(
                StructurePlaceholderComponent.class);
            if (structurePlaceholderComponent.selectedPrefab == null) {
                continue;
            }
            BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
            ScheduleStructurePlacementComponent.PlacementToSchedule placementToSchedule = new ScheduleStructurePlacementComponent.PlacementToSchedule();
            placementToSchedule.position = transformToRelative.transformVector3i(blockComponent.getPosition(new Vector3i()));
            placementToSchedule.position.y -= 1; // placeholder is on top of marked block
            placementToSchedule.front = transformToRelative.transformSide(blockComponent.getBlock().getDirection());
            placementToSchedule.structureTemplateType = structurePlaceholderComponent.selectedPrefab;
            placementToSchedules.add(placementToSchedule);
        }
        if (placementToSchedules.size() > 0) {
            ScheduleStructurePlacementComponent scheduleStructurePlacementComponent = new ScheduleStructurePlacementComponent();
            scheduleStructurePlacementComponent.placementsToSchedule = placementToSchedules;
            event.getTemplateEntity().addOrSaveComponent(scheduleStructurePlacementComponent);
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onBuildTemplateComponentString(BuildStructureTemplateStringEvent event, EntityRef template,
                                               StructureTemplateComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"StructureTemplate\": {\n");
        boolean firstProperty = true;
        if (component.type != null) {
            sb.append("        \"type\": \"");
            sb.append(component.type.getUrn().toString());
            sb.append("\"");
            firstProperty = false;
        }
        StructureTemplateComponent defaultComponent = new StructureTemplateComponent();
        if (component.spawnChance != defaultComponent.spawnChance) {
            if (!firstProperty) {
                sb.append(",\n");
            }
            sb.append("        \"spawnChance\": ");
            sb.append(Integer.toString(component.spawnChance));
            firstProperty = false;
        }
        if (!firstProperty) {
            sb.append("\n");
        }
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), StructureTemplateComponent.class);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      SpawnBlockRegionsComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"SpawnBlockRegions\": {\n");
        sb.append("        \"regionsToFill\": [\n");
        sb.append(formatAsString(component.regionsToFill));
        sb.append("        ]\n");
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), SpawnBlockRegionsComponent.class);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      FallingBlocksPlacementAlgorithmComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"FallingBlockPlacementAlgorithm\": {\n");
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), FallingBlocksPlacementAlgorithmComponent.class);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      NoConstructionAnimationComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"NoConstructionAnimation\": {\n");
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), NoConstructionAnimationComponent.class);
    }

    @ReceiveEvent
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      ScheduleStructurePlacementComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"ScheduleStructurePlacement\": {\n");
        sb.append("        \"placementsToSchedule\": [\n");
        ListUtil.visitList(component.placementsToSchedule,
            (ScheduleStructurePlacementComponent.PlacementToSchedule placementToSchedule, boolean last) -> {
                sb.append("            {\n");
                sb.append("                \"structureTemplateType\": \"");
                sb.append(placementToSchedule.structureTemplateType.getUrn().toString());
                sb.append("\",\n");
                sb.append("                \"front\": \"");
                sb.append(placementToSchedule.front.name());
                sb.append("\",\n");
                sb.append("                \"position\": [");
                sb.append(placementToSchedule.position.x);
                sb.append(", ");
                sb.append(placementToSchedule.position.y);
                sb.append(", ");
                sb.append(placementToSchedule.position.z);
                sb.append("]\n");
                if (last) {
                    sb.append("        }\n");
                } else {
                    sb.append("        },\n");
                }
            });
        sb.append("        ]\n");
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), ScheduleStructurePlacementComponent.class);

    }

    @ReceiveEvent
    public void onCopyBlockRegionRequest(StructureTemplateStringRequest event, EntityRef characterEntity,
                                         CharacterComponent characterComponent) {
        EntityRef structureTemplateOriginEntity = characterComponent.authorizedInteractionTarget;
        StructureTemplateOriginComponent structureTemplateOriginComponent = structureTemplateOriginEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            LOGGER.error("Ignored StructureTemplateStringRequest event since there was no interaction with a structure template origin block");
            return;
        }

        BlockComponent blockComponent = structureTemplateOriginEntity.getComponent(BlockComponent.class);
        if (blockComponent == null) {
            LOGGER.error("Structure template origin was not a block, ignoring event");
            return;
        }

        EntityBuilder entityBuilder = entityManager.newBuilder();
        addComponentsToTemplate(structureTemplateOriginEntity, structureTemplateOriginComponent, blockComponent, entityBuilder);
        EntityRef templateEntity = entityBuilder.build();
        BuildStructureTemplateStringEvent buildStringEvent = new BuildStructureTemplateStringEvent();
        templateEntity.send(buildStringEvent);
        String textToSend = buildStringEvent.getMap().values().stream().collect(Collectors.joining(",\n", "{\n", "\n}\n"));
        templateEntity.destroy();

        CopyBlockRegionResultEvent resultEvent = new CopyBlockRegionResultEvent(textToSend);
        characterEntity.send(resultEvent);
    }


    @ReceiveEvent
    public void onMakeBoxShapedRequest(MakeBoxShapedRequest event, EntityRef characterEntity,
                                       CharacterComponent characterComponent) {
        EntityRef structureTemplateOriginEntity = characterComponent.authorizedInteractionTarget;
        StructureTemplateOriginComponent structureTemplateOriginComponent = structureTemplateOriginEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            LOGGER.error("Ignored MakeBoxShapedRequest event since there was no interaction with a structure template origin block");
            return;
        }
        structureTemplateOriginComponent.absoluteTemplateRegions.clear();
        structureTemplateOriginComponent.absoluteTemplateRegions.add(new BlockRegion(event.getRegion()));
        structureTemplateOriginEntity.saveComponent(structureTemplateOriginComponent);
    }


    @ReceiveEvent(components = {BlockItemComponent.class})
    public void onBlockItemPlaced(OnBlockItemPlaced event, EntityRef itemEntity,
                                  StructureTemplateOriginComponent editorComponentOfItem) {
        EntityRef placedBlockEntity = event.getPlacedBlock();

        StructureTemplateOriginComponent editorComponentOfBlock = placedBlockEntity.getComponent(StructureTemplateOriginComponent.class);
        if (editorComponentOfBlock == null) {
            editorComponentOfBlock = new StructureTemplateOriginComponent();
        }
        editorComponentOfBlock.absoluteTemplateRegions = new ArrayList<>(editorComponentOfItem.absoluteTemplateRegions);
        placedBlockEntity.saveComponent(editorComponentOfBlock);

        StructureTemplateComponent structureTemplateComponentOfItem = itemEntity.getComponent(StructureTemplateComponent.class);
        if (structureTemplateComponentOfItem == null) {
            structureTemplateComponentOfItem = new StructureTemplateComponent();
        }
        placedBlockEntity.addOrSaveComponent(componentLibrary.copy(structureTemplateComponentOfItem));
    }

    @ReceiveEvent(components = {})
    public void onBlockToItem(OnBlockToItem event, EntityRef blockEntity,
                              StructureTemplateOriginComponent componentOfBlock) {
        EntityRef item = event.getItem();
        StructureTemplateOriginComponent componentOfItem = item.getComponent(StructureTemplateOriginComponent.class);
        if (componentOfItem == null) {
            componentOfItem = new StructureTemplateOriginComponent();
        }
        componentOfItem.absoluteTemplateRegions = new ArrayList<>(componentOfBlock.absoluteTemplateRegions);
        item.saveComponent(componentOfItem);

        StructureTemplateComponent structureTemplateComponentOfBlock = blockEntity.getComponent(StructureTemplateComponent.class);
        if (structureTemplateComponentOfBlock == null) {
            structureTemplateComponentOfBlock = new StructureTemplateComponent();
        }
        item.addOrSaveComponent(componentLibrary.copy(structureTemplateComponentOfBlock));
    }

    private Map<Block, Set<Vector3i>> createBlockToAbsolutePositionsMap(
        StructureTemplateOriginComponent structureTemplateOriginComponent) {
        List<BlockRegion> absoluteRegions = structureTemplateOriginComponent.absoluteTemplateRegions;

        Map<Block, Set<Vector3i>> map = new HashMap<>();
        for (BlockRegion absoluteRegion : absoluteRegions) {
            for (Vector3ic absolutePosition : absoluteRegion) {
                Block block = worldProvider.getBlock(absolutePosition);
                Set<Vector3i> positions = map.get(block);
                if (positions == null) {
                    positions = new HashSet<>();
                    map.put(block, positions);
                }
                positions.add(new Vector3i(absolutePosition));
            }
        }
        return map;
    }


    private List<RegionToFill> createRegionsToFill(StructureTemplateOriginComponent structureTemplateOriginComponent,
                                                   BlockRegionTransform transformToRelative) {
        List<BlockRegion> absoluteRegions = structureTemplateOriginComponent.absoluteTemplateRegions;

        List<RegionToFill> regionsToFill = new ArrayList<>();
        for (BlockRegion absoluteRegion : absoluteRegions) {
            for (Vector3ic absolutePosition : absoluteRegion) {
                EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(absolutePosition);
                BlockPlaceholderComponent placeholderComponent = blockEntity.getComponent(BlockPlaceholderComponent.class);
                Block block;
                if (placeholderComponent != null) {
                    block = placeholderComponent.block;
                } else {
                    block = worldProvider.getBlock(absolutePosition);
                }
                if (block == null) {
                    continue;
                }
                Block relativeBlock = transformToRelative.transformBlock(block);
                RegionToFill regionToFill = new RegionToFill();
                Vector3i relativePosition = transformToRelative.transformVector3i(new Vector3i(absolutePosition));
                regionToFill.region = new BlockRegion(relativePosition);
                regionToFill.blockType = relativeBlock;
                regionsToFill.add(regionToFill);


            }
        }
        RegionMergeUtil.mergeRegionsToFill(regionsToFill);
        regionsToFill.sort(REGION_BY_BLOCK_TYPE_COMPARATOR.thenComparing(REGION_BY_MIN_Z_COMPARATOR)
            .thenComparing(REGION_BY_MIN_X_COMPARATOR).thenComparing(REGION_BY_MIN_Y_COMPARATOR));
        return regionsToFill;
    }


    // TODO move 2 methods to utility class
    public static BlockRegionTransform createAbsoluteToRelativeTransform(BlockComponent blockComponent) {
        Side front = blockComponent.getBlock().getDirection();
        Vector3i minusOrigin = new Vector3i(0, 0, 0);
        minusOrigin.sub(blockComponent.getPosition(new Vector3i()));
        return BlockRegionTransform.createMovingThenRotating(minusOrigin, front, Side.FRONT);
    }

    public static BlockRegionTransform createRelativeToAbsoluteTransform(BlockComponent blockComponent) {
        Side front = blockComponent.getBlock().getDirection();
        return BlockRegionTransform.createRotationThenMovement(Side.FRONT, front, blockComponent.getPosition(new Vector3i()));
    }


    static String formatAsString(List<RegionToFill> regionsToFill) {
        StringBuilder sb = new StringBuilder();
        ListUtil.visitList(regionsToFill, (RegionToFill regionToFill, boolean last) -> {
            sb.append("            { \"blockType\": \"");
            sb.append(regionToFill.blockType);
            sb.append("\", \"region\": { \"min\": [");
            sb.append(regionToFill.region.minX());
            sb.append(", ");
            sb.append(regionToFill.region.minY());
            sb.append(", ");
            sb.append(regionToFill.region.minZ());
            sb.append("], \"size\": [");
            sb.append(regionToFill.region.getSizeX());
            sb.append(", ");
            sb.append(regionToFill.region.getSizeY());
            sb.append(", ");
            sb.append(regionToFill.region.getSizeZ());
            if (last) {
                sb.append("]}}\n");
            } else {
                sb.append("]}},\n");
            }
        });
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

        Vector3i position = blockComponent.getPosition(new Vector3i());
        Vector3f directionVector = event.getDirection();
        Side directionStructureIsIn = Side.inHorizontalDirection(directionVector.x(), directionVector.z());
        Side frontDirectionOfStructure = directionStructureIsIn.reverse();


        List<BlockRegion> absoluteRegions = getAbsolutePlacementRegionsOfTemplate(entity, position, frontDirectionOfStructure);


        BlockRegionTransform blockRegionTransform = StructureSpawnServerSystem.getBlockRegionTransformForStructurePlacement(
            event, blockComponent);
        entity.send(new SpawnTemplateEvent(blockRegionTransform));

        StructureTemplateComponent structureTemplateComponentOfItem = entity.getComponent(StructureTemplateComponent.class);
        StructureTemplateComponent newStructureTemplateComponent = componentLibrary.copy(structureTemplateComponentOfItem);
        placeOriginMarkerWithTemplateData(event, position, frontDirectionOfStructure, absoluteRegions,
            newStructureTemplateComponent);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_NORMAL)
    public void onSpawnTemplateEventWithPlaceholderPriority(SpawnTemplateEvent event, EntityRef entity,
                                                            ScheduleStructurePlacementComponent placementComponent) {
        BlockRegionTransform transformation = event.getTransformation();
        for (ScheduleStructurePlacementComponent.PlacementToSchedule placementToSchedule : placementComponent.placementsToSchedule) {
            Vector3i actualPosition = transformation.transformVector3i(placementToSchedule.position);
            Side side = transformation.transformSide(placementToSchedule.front);
            Prefab selectedTemplateType = placementToSchedule.structureTemplateType;

            BlockFamily blockFamily = blockManager.getBlockFamily("StructureTemplates:StructurePlaceholder");
            HorizontalFamily horizontalBlockFamily = (HorizontalFamily) blockFamily;
            Block block = horizontalBlockFamily.getBlockForSide(side);
            Vector3i positionAbove = new Vector3i(actualPosition);
            positionAbove.y += 1;
            worldProvider.setBlock(positionAbove, block);
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(positionAbove);
            StructurePlaceholderComponent structurePlaceholderComponent = blockEntity.getComponent(StructurePlaceholderComponent.class);
            structurePlaceholderComponent.selectedPrefab = selectedTemplateType;
            blockEntity.saveComponent(structurePlaceholderComponent);
        }

    }


    List<BlockRegion> getAbsolutePlacementRegionsOfTemplate(EntityRef entity, Vector3i position, Side frontDirectionOfStructure) {
        List<BlockRegion> relativeRegions = getPlacementRegionsOfTemplate(entity);

        BlockRegionTransform rotation = BlockRegionTransform.createRotationThenMovement(Side.FRONT,
            frontDirectionOfStructure, position);
        List<BlockRegion> absoluteRegions = new ArrayList<>();
        for (BlockRegion relativeRegion : relativeRegions) {
            BlockRegion absoluteRegion = rotation.transformRegion(relativeRegion);
            absoluteRegions.add(absoluteRegion);
        }

        Set<Vector3ic> positionsInTemplate = Sets.newHashSet();
        positionsInTemplate.addAll(RegionMergeUtil.positionsOfRegions(absoluteRegions));
        List<BlockRegion> newTemplateRegions = RegionMergeUtil.mergePositionsIntoRegions(positionsInTemplate);
        return newTemplateRegions;
    }

    private List<BlockRegion> getPlacementRegionsOfTemplate(EntityRef entity) {
        List<BlockRegion> relativeRegions = Lists.newArrayList();
        SpawnBlockRegionsComponent blockRegionsComponent = entity.getComponent(SpawnBlockRegionsComponent.class);
        if (blockRegionsComponent != null) {
            for (RegionToFill regionToFill : blockRegionsComponent.regionsToFill) {
                relativeRegions.add(regionToFill.region);
            }
        }
        return relativeRegions;
    }

    private BlockRegion getPlacementBoundingsOfTemplate(EntityRef entity) {
        BlockRegion unrotatedRegion = null;
        SpawnBlockRegionsComponent blockRegionsComponent = entity.getComponent(SpawnBlockRegionsComponent.class);
        if (blockRegionsComponent != null) {
            for (RegionToFill regionToFill : blockRegionsComponent.regionsToFill) {
                if (unrotatedRegion == null) {
                    unrotatedRegion = regionToFill.region;
                } else {
                    unrotatedRegion.union(regionToFill.region.getMin(new Vector3i()));
                    unrotatedRegion.union(regionToFill.region.getMax(new Vector3i()));
                }
            }
        }
        if (unrotatedRegion == null) {
            unrotatedRegion.setMin(0, 0, 0).setMax(0, 0, 0);
        }
        return unrotatedRegion;
    }

    boolean placeOriginMarkerWithTemplateData(ActivateEvent event, Vector3i position, Side frontDirectionOfStructure,
                                              List<BlockRegion> regions,
                                              StructureTemplateComponent newStructureTemplateComponent) {
        boolean originPlaced = placeOriginMarkerBlockWithoutData(event, position, frontDirectionOfStructure);
        if (!originPlaced) {
            LOGGER.info("Structure template origin placement got denied");
            return false;
        }
        EntityRef originBlockEntity = blockEntityRegistry.getBlockEntityAt(position);
        addTemplateDataToBlockEntity(position, regions, originBlockEntity, newStructureTemplateComponent);
        return true;
    }

    private void addTemplateDataToBlockEntity(Vector3i position, List<BlockRegion> regions, EntityRef originBlockEntity,
                                              StructureTemplateComponent newStructureTemplateComponent) {
        StructureTemplateOriginComponent editorComponent = originBlockEntity.getComponent(StructureTemplateOriginComponent.class);
        if (editorComponent != null) {
            editorComponent.absoluteTemplateRegions.clear();
            editorComponent.absoluteTemplateRegions.addAll(regions);
            originBlockEntity.saveComponent(editorComponent);
        }
        originBlockEntity.addOrSaveComponent(newStructureTemplateComponent);
    }

    private boolean placeOriginMarkerBlockWithoutData(ActivateEvent event, Vector3i position, Side frontDirectionOfStructure) {
        BlockFamily blockFamily = blockManager.getBlockFamily("StructureTemplates:StructureTemplateOrigin");
        HorizontalFamily horizontalBlockFamily = (HorizontalFamily) blockFamily;
        Block block = horizontalBlockFamily.getBlockForSide(frontDirectionOfStructure);

        PlaceBlocks placeBlocks = new PlaceBlocks(position, block, event.getInstigator());
        worldProvider.getWorldEntity().send(placeBlocks);
        return !placeBlocks.isConsumed();
    }

}
