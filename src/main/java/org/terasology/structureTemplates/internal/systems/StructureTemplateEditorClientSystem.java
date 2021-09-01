// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.clipboard.ClipboardManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.RegionOutlineComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.structureTemplates.internal.components.EditTemplateRegionProcessComponent;
import org.terasology.structureTemplates.internal.components.EditingUserComponent;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionResultEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the result of the activation of the copyBlockRegionTool item.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class StructureTemplateEditorClientSystem extends BaseComponentSystem {

    @In
    private ClipboardManager clipboardManager;

    @In
    private LocalPlayer locatPlayer;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private List<EntityRef> regionOutlineEntities = new ArrayList<>();
    private EntityRef highlightedEditorEntity = EntityRef.NULL;


    @ReceiveEvent
    public void onCopyBlockRegionResultEvent(CopyBlockRegionResultEvent event, EntityRef entity) {
        clipboardManager.setClipboardContents(event.getJson());
    }

    @ReceiveEvent
    public void onAddedCopyBlockRegionComponent(OnAddedComponent event, EntityRef entity,
                                                StructureTemplateOriginComponent component) {
        updateHighlightedEditorEntity();
    }

    @ReceiveEvent
    public void onChangedCopyBlockRegionComponent(OnChangedComponent event, EntityRef entity,
                                             StructureTemplateOriginComponent component) {
        updateHighlightedEditorEntity();
    }

    @ReceiveEvent
    public void onBeforeRemoveCopyBlockRegionComponent(BeforeRemoveComponent event, EntityRef entity,
                                                  StructureTemplateOriginComponent component) {
        if (entity.equals(highlightedEditorEntity)) {
            setHighlightedEditorEntity(EntityRef.NULL);
        }
        // Calling updateOutlineEntity is not possible as component is still there
    }


    @ReceiveEvent
    public void onAddedEditsCopyRegionComponent(OnAddedComponent event, EntityRef entity,
                                                EditingUserComponent component) {
        updateHighlightedEditorEntity();
    }

    @ReceiveEvent
    public void onChangedEditsCopyRegionComponent(OnChangedComponent event, EntityRef entity,
                                                  EditingUserComponent component) {
        updateHighlightedEditorEntity();
    }

    @ReceiveEvent
    public void onBeforeRemoveEditsCopyRegionComponent(BeforeRemoveComponent event, EntityRef entity,
                                                       EditingUserComponent component) {
        if (entity.equals(locatPlayer.getClientEntity())) {
            setHighlightedEditorEntity(EntityRef.NULL);
        }
    }

    public void destoryOutlineEntities() {
        for (EntityRef regionOutlineEntity : regionOutlineEntities) {
            if (regionOutlineEntity.exists()) {
                regionOutlineEntity.destroy();
            }
        }
    }

    public void updateOutlineEntities() {
            List<BlockRegion> regionsToDraw = getRegionsToDraw();
        destoryOutlineEntities();

        for (BlockRegion regionToDraw: regionsToDraw) {
            EntityBuilder entityBuilder = entityManager.newBuilder();
            entityBuilder.setPersistent(false);
            RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
            regionOutlineComponent.corner1 = new Vector3i(regionToDraw.getMin(new Vector3i()));
            regionOutlineComponent.corner2 = new Vector3i(regionToDraw.getMax(new Vector3i()));
            entityBuilder.addComponent(regionOutlineComponent);
            regionOutlineEntities.add(entityBuilder.build());
        }
    }


    private void setHighlightedEditorEntity(EntityRef entityRef) {
        highlightedEditorEntity = entityRef;
        updateOutlineEntities();
    }

    private void updateHighlightedEditorEntity() {
        setHighlightedEditorEntity(determineEditorEntityToHighLight());
    }

    private EntityRef determineEditorEntityToHighLight() {
        EntityRef clientEntity = locatPlayer.getClientEntity();

        EditingUserComponent editingUserComponent = clientEntity.getComponent(EditingUserComponent.class);
        if (editingUserComponent == null) {
            return EntityRef.NULL;
        }

        EntityRef editProcessEntity = editingUserComponent.editProcessEntity;
        EditTemplateRegionProcessComponent editTemplateRegionProcessComponent = editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
        if (editTemplateRegionProcessComponent == null) {
            return EntityRef.NULL;
        }
        return editTemplateRegionProcessComponent.structureTemplateEditor;
    }


    private List<BlockRegion> getRegionsToDraw() {

        StructureTemplateOriginComponent structureTemplateOriginComponent = highlightedEditorEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(structureTemplateOriginComponent.absoluteTemplateRegions);
    }
}
