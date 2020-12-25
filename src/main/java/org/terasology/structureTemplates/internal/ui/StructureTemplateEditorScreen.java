// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UICheckbox;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.structureTemplates.internal.components.EditTemplateRegionProcessComponent;
import org.terasology.structureTemplates.internal.components.EditingUserComponent;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;
import org.terasology.structureTemplates.internal.events.CreateEditTemplateRegionProcessRequest;
import org.terasology.structureTemplates.internal.events.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.internal.events.CreateStructureTemplateItemRequest;
import org.terasology.structureTemplates.internal.events.MakeBoxShapedRequest;
import org.terasology.structureTemplates.internal.events.StopEditingProcessRequest;
import org.terasology.structureTemplates.internal.events.StructureTemplateStringRequest;
import org.terasology.structureTemplates.internal.systems.StructureTemplateEditorServerSystem;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.structureTemplates.util.ListUtil;
import org.terasology.structureTemplates.util.RegionMergeUtil;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegion;

import java.util.List;
import java.util.Set;

/**
 * Main structure template editor UI
 */
public class StructureTemplateEditorScreen extends BaseInteractionScreen {
    private UIButton editTemplatePropertiesButton;
    private UIButton copyToClipboardButton;
    private UIButton createSpawnerButton;
    private UIButton createTemplateButton;
    private UIButton copyInGroundConditionButton;
    private UICheckbox recordBlockAdditionCheckBox;
    private UICheckbox recordBlockRemovalCheckBox;

    @In
    private ClipboardManager clipboardManager;

    @In
    private EntityManager entityManager;


    @In
    private LocalPlayer localPlayer;
    private UIButton makeBoxShapedButton;

    private boolean recordBlockAddition;
    private boolean recordBlockRemoval;


    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        EditTemplateRegionProcessComponent editProcessComponent = findEditProcessForInteractionTarget();
        if (editProcessComponent != null) {
            recordBlockAddition = editProcessComponent.recordBlockAddition;
            recordBlockRemoval = editProcessComponent.recordBlockRemoval;
        } else {
            recordBlockAddition = false;
            recordBlockRemoval = false;
        }

    }

    @Override
    public void initialise() {
        editTemplatePropertiesButton = find("editTemplatePropertiesButton", UIButton.class);
        if (editTemplatePropertiesButton != null) {
            editTemplatePropertiesButton.subscribe(this::onEditTemplatePropertiesButton);
        }
        copyToClipboardButton = find("copyToClipboardButton", UIButton.class);
        if (copyToClipboardButton != null) {
            copyToClipboardButton.subscribe(this::onCopyToClipboardClicked);
        }

        createSpawnerButton = find("createSpawnerButton", UIButton.class);
        if (createSpawnerButton != null) {
            createSpawnerButton.subscribe(this::onCreateSpawnerButton);
        }

        createTemplateButton = find("createTemplateButton", UIButton.class);
        if (createTemplateButton != null) {
            createTemplateButton.subscribe(this::onCreateTemplateItemButton);
        }

        copyInGroundConditionButton = find("copyInGroundConditionButton", UIButton.class);
        if (copyInGroundConditionButton != null) {
            copyInGroundConditionButton.subscribe(this::onCopyInGroundConditionButton);
        }

        makeBoxShapedButton = find("makeBoxShapedButton", UIButton.class);
        if (makeBoxShapedButton != null) {
            makeBoxShapedButton.subscribe(this::onMakeBoxShapedButton);
        }

        recordBlockAdditionCheckBox = find("recordBlockAdditionCheckBox", UICheckbox.class);
        recordBlockRemovalCheckBox = find("recordBlockRemovalCheckBox", UICheckbox.class);
        if (recordBlockAdditionCheckBox != null && recordBlockRemovalCheckBox != null) {
            recordBlockAdditionCheckBox.bindChecked(
                    new Binding<Boolean>() {
                        @Override
                        public Boolean get() {
                            return recordBlockAddition;
                        }

                        @Override
                        public void set(Boolean value) {
                            recordBlockAddition = value;
                            updateBlockEditingProcessOnServer();
                        }

                    });
            recordBlockRemovalCheckBox.bindChecked(
                    new Binding<Boolean>() {
                        @Override
                        public Boolean get() {
                            return recordBlockRemoval;
                        }

                        @Override
                        public void set(Boolean value) {
                            recordBlockRemoval = value;
                            updateBlockEditingProcessOnServer();
                        }

                    });
        }

    }

    /**
     * Can return null.
     */
    private EditTemplateRegionProcessComponent findEditProcessForInteractionTarget() {
        EntityRef client = localPlayer.getClientEntity();
        EditingUserComponent editingUserComponent = client.getComponent(EditingUserComponent.class);
        if (editingUserComponent == null) {
            return null;
        }
        EditTemplateRegionProcessComponent editProcessComponent =
                editingUserComponent.editProcessEntity.getComponent(EditTemplateRegionProcessComponent.class);
        if (!editProcessComponent.structureTemplateEditor.equals(getInteractionTarget())) {
            return null;
        }
        return editProcessComponent;
    }

    private void updateBlockEditingProcessOnServer() {
        if (recordBlockAddition || recordBlockRemoval) {
            localPlayer.getCharacterEntity().send(new CreateEditTemplateRegionProcessRequest(recordBlockAddition,
                    recordBlockRemoval));
        } else {
            localPlayer.getClientEntity().send(new StopEditingProcessRequest());
        }
    }

    private void onEditTemplatePropertiesButton(UIWidget button) {
        StructureTemplatePropertiesScreen screen = getManager().pushScreen("StructureTemplates" +
                ":StructureTemplatePropertiesScreen", StructureTemplatePropertiesScreen.class);
        screen.setEditorEntity(getInteractionTarget());
    }


    private void onMakeBoxShapedButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateOriginComponent component = entity.getComponent(StructureTemplateOriginComponent.class);
        //TODO: The region might be invalid (empty) - what should we do in this case?
        BlockRegion absoluteRegion = getBoundingRegion(component.absoluteTemplateRegions);
        StructureTemplateRegionScreen regionScreen = getManager().pushScreen(
                "StructureTemplates:StructureTemplateRegionScreen", StructureTemplateRegionScreen.class);

        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        BlockRegionTransform transformToRelative =
                StructureTemplateEditorServerSystem.createAbsoluteToRelativeTransform(blockComponent);
        BlockRegionTransform transformToAbsolute =
                StructureTemplateEditorServerSystem.createRelativeToAbsoluteTransform(blockComponent);

        BlockRegion relativeRegion = transformToRelative.transformRegion(absoluteRegion);
        regionScreen.setRegion(relativeRegion);
        regionScreen.setOkHandler((BlockRegion relativeNewRegion) -> {
            BlockRegion absoluteNewRegion = transformToAbsolute.transformRegion(relativeNewRegion);
            localPlayer.getCharacterEntity().send(new MakeBoxShapedRequest(absoluteNewRegion));
        });
    }

    /**
     * Compute the bounding box over the given regions, i.e., build the union of the given regions.
     *
     * @return the union of all regions; an invalid region of {@code regions} is empty
     */
    private BlockRegion getBoundingRegion(List<BlockRegion> regions) {
        return regions.stream().reduce(new BlockRegion(BlockRegion.INVALID), BlockRegion::union, BlockRegion::union);
    }

    // TODO add item that can do this job or introduce a better way that makes it superflous
    private void onCopyInGroundConditionButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateOriginComponent component = entity.getComponent(StructureTemplateOriginComponent.class);
        List<BlockRegion> regionsOneHigher = Lists.newArrayList();

        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        BlockRegionTransform transformToRelative =
                StructureTemplateEditorServerSystem.createAbsoluteToRelativeTransform(blockComponent);


        for (BlockRegion absoluteRegion : component.absoluteTemplateRegions) {
            BlockRegion relativeRegion = transformToRelative.transformRegion(absoluteRegion);
            Vector3i max = new Vector3i(relativeRegion.getMax(new Vector3i()));
            max.add(0, 1, 0);
            regionsOneHigher.add(new BlockRegion(relativeRegion.getMin(new Vector3i()), max));
        }

        Set<Vector3i> positions = RegionMergeUtil.positionsOfRegions(regionsOneHigher);
        List<BlockRegion> regions = RegionMergeUtil.mergePositionsIntoRegions(positions);

        String string = formatRegionsAsGroundCondition(regions);
        clipboardManager.setClipboardContents(string);
    }

    private String formatRegionsAsGroundCondition(List<BlockRegion> regions) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("    \"CheckBlockRegionCondition\" : {\n");
        stringBuilder.append("        \"checksToPerform\": [\n");
        ListUtil.visitList(regions, (BlockRegion region, boolean last) -> {
            stringBuilder.append(String.format(
                    "            {\"condition\": \"StructureTemplates:IsGroundLike\", \"region\" :{\"min\": [%d, %d, " +
                            "%d], \"size\": [%d, %d, %d]}}",
                    region.minX(), region.minY(), region.minZ(), region.getSizeX(), region.getSizeY(), region.getSizeZ()));
            if (last) {
                stringBuilder.append("\n");
            } else {
                stringBuilder.append(",\n");
            }
        });
        stringBuilder.append("        ]\n");
        stringBuilder.append("    },\n");
        return stringBuilder.toString();
    }

    private void onCreateSpawnerButton(UIWidget button) {
        localPlayer.getCharacterEntity().send(new CreateStructureSpawnItemRequest());
    }

    private void onCreateTemplateItemButton(UIWidget button) {
        localPlayer.getCharacterEntity().send(new CreateStructureTemplateItemRequest());
    }

    private void onCopyToClipboardClicked(UIWidget button) {
        localPlayer.getCharacterEntity().send(new StructureTemplateStringRequest());
    }

}
