// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

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
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.RegionOutlineComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.math.geom.Vector3i;
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

    private final List<EntityRef> regionOutlineEntities = new ArrayList<>();
    @In
    private ClipboardManager clipboardManager;
    @In
    private LocalPlayer locatPlayer;
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;
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
        List<Region3i> regionsToDraw = getRegionsToDraw();
        destoryOutlineEntities();

        for (Region3i regionToDraw : regionsToDraw) {
            EntityBuilder entityBuilder = entityManager.newBuilder();
            entityBuilder.setPersistent(false);
            RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
            regionOutlineComponent.corner1 = new Vector3i(regionToDraw.min());
            regionOutlineComponent.corner2 = new Vector3i(regionToDraw.max());
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
        EditTemplateRegionProcessComponent editTemplateRegionProcessComponent =
                editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
        if (editTemplateRegionProcessComponent == null) {
            return EntityRef.NULL;
        }
        return editTemplateRegionProcessComponent.structureTemplateEditor;
    }


    private List<Region3i> getRegionsToDraw() {

        StructureTemplateOriginComponent structureTemplateOriginComponent =
                highlightedEditorEntity.getComponent(StructureTemplateOriginComponent.class);
        if (structureTemplateOriginComponent == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(structureTemplateOriginComponent.absoluteTemplateRegions);
    }
}
