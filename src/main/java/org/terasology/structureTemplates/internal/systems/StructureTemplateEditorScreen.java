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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.structureTemplates.internal.components.EditsCopyRegionComponent;
import org.terasology.structureTemplates.internal.events.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.internal.events.MakeBoxShapedRequest;
import org.terasology.structureTemplates.internal.events.StructureTemplateStringRequest;
import org.terasology.structureTemplates.internal.ui.StructureTemplatePropertiesScreen;
import org.terasology.structureTemplates.internal.ui.StructureTemplateRegionScreen;
import org.terasology.structureTemplates.util.ListUtil;
import org.terasology.structureTemplates.util.RegionMergeUtil;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.block.BlockComponent;

import java.util.List;
import java.util.Set;

/**
 * Main structure template editor UI
 */
public class StructureTemplateEditorScreen extends BaseInteractionScreen {
    private UIButton editTemplatePropertiesButton;
    private UIButton copyToClipboardButton;
    private UIButton createSpawnerButton;
    private UIButton copyInGroundConditionButton;
    private UICheckbox editCopyRegionsCheckBox;

    @In
    private ClipboardManager clipboardManager;


    @In
    private LocalPlayer localPlayer;
    private UIButton makeBoxShapedButton;


    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        // nothing to do
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

        copyInGroundConditionButton = find("copyInGroundConditionButton", UIButton.class);
        if (copyInGroundConditionButton != null) {
            copyInGroundConditionButton.subscribe(this::onCopyInGroundConditionButton);
        }

        makeBoxShapedButton = find("makeBoxShapedButton", UIButton.class);
        if (makeBoxShapedButton != null) {
            makeBoxShapedButton.subscribe(this::onMakeBoxShapedButton);
        }

        editCopyRegionsCheckBox = find("editCopyRegionsCheckBox", UICheckbox.class);
        if (editCopyRegionsCheckBox != null) {
            editCopyRegionsCheckBox.bindChecked(
                    new Binding<Boolean>() {
                        @Override
                        public Boolean get() {
                            EntityRef client = localPlayer.getClientEntity();
                            EditsCopyRegionComponent component = client.getComponent(EditsCopyRegionComponent.class);
                            if (component == null) {
                                return Boolean.FALSE;
                            }
                            return (component.structureTemplateEditor.equals(getInteractionTarget()));
                        }

                        @Override
                        public void set(Boolean value) {
                            EntityRef client = localPlayer.getClientEntity();
                            if (Boolean.TRUE.equals(value)) {

                                EditsCopyRegionComponent component = client.getComponent(EditsCopyRegionComponent.class);
                                if (component == null) {
                                    component = new EditsCopyRegionComponent();
                                }
                                component.structureTemplateEditor = getInteractionTarget();
                                client.addOrSaveComponent(component);
                            } else {
                                EditsCopyRegionComponent component = client.getComponent(EditsCopyRegionComponent.class);
                                if (component.structureTemplateEditor.equals(getInteractionTarget())) {
                                    client.removeComponent(EditsCopyRegionComponent.class);
                                }
                            }
                        }
            });
        }

    }
    private void onEditTemplatePropertiesButton(UIWidget button) {
        StructureTemplatePropertiesScreen screen = getManager().pushScreen("StructureTemplates:StructureTemplatePropertiesScreen", StructureTemplatePropertiesScreen.class);
        screen.setEditorEntity(getInteractionTarget());
    }


    private void onMakeBoxShapedButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateOriginComponent component = entity.getComponent(StructureTemplateOriginComponent.class);
        Region3i absoluteRegion = getBoundingRegion(component.absoluteTemplateRegions);
        StructureTemplateRegionScreen regionScreen = getManager().pushScreen(
                "StructureTemplates:StructureTemplateRegionScreen", StructureTemplateRegionScreen.class);

        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        BlockRegionTransform transformToRelative = StructureTemplateEditorServerSystem.createAbsoluteToRelativeTransform(blockComponent);
        BlockRegionTransform transformToAbsolute = StructureTemplateEditorServerSystem.createRelativeToAbsoluteTransform(blockComponent);

        Region3i relativeRegion = transformToRelative.transformRegion(absoluteRegion);
        regionScreen.setRegion(relativeRegion);
        regionScreen.setOkHandler((Region3i relativeNewRegion) -> {
            Region3i absoluteNewRegion = transformToAbsolute.transformRegion(relativeNewRegion);
            getInteractionTarget().send(new MakeBoxShapedRequest(localPlayer.getCharacterEntity(), absoluteNewRegion));
        });
    }

    private Region3i getBoundingRegion(List<Region3i> regions) {
        if (regions.size() == 0) {
            return Region3i.EMPTY;
        }
        Vector3i min = new Vector3i(regions.get(0).min());
        Vector3i max = new Vector3i(regions.get(0).max());
        for (Region3i region: regions) {
            min.min(region.min());
            max.max(region.max());
        }
        return Region3i.createFromMinMax(min, max);
    }

    // TODO add item that can do this job or introduce a better way that makes it superflous
    private void onCopyInGroundConditionButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateOriginComponent component = entity.getComponent(StructureTemplateOriginComponent.class);
        List<Region3i> regionsOneHigher = Lists.newArrayList();

        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        BlockRegionTransform transformToRelative = StructureTemplateEditorServerSystem.createAbsoluteToRelativeTransform(blockComponent);


        for (Region3i absoluteRegion: component.absoluteTemplateRegions) {
            Region3i relativeRegion = transformToRelative.transformRegion(absoluteRegion);
            Vector3i max = new Vector3i(relativeRegion.max());
            max.addY(1);
            regionsOneHigher.add(Region3i.createFromMinMax(relativeRegion.min(), max));
        }

        Set<Vector3i> positions = RegionMergeUtil.positionsOfRegions(regionsOneHigher);
        List<Region3i> regions = RegionMergeUtil.mergePositionsIntoRegions(positions);

        String string = formatRegionsAsGroundCondition(regions);
        clipboardManager.setClipboardContents(string);
    }

    private String formatRegionsAsGroundCondition(List<Region3i> regions) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("    \"CheckBlockRegionCondition\" : {\n");
        stringBuilder.append("        \"checksToPerform\": [\n");
        ListUtil.visitList(regions, (Region3i region, boolean last) -> {
            stringBuilder.append(String.format(
                    "            {\"condition\": \"StructureTemplates:IsGroundLike\", \"region\" :{\"min\": [%d, %d, %d], \"size\": [%d, %d, %d]}}",
                    region.minX(),region.minY(), region.minZ(),region.sizeX(), region.sizeY(), region.sizeZ()));
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
        getInteractionTarget().send(new CreateStructureSpawnItemRequest(localPlayer.getCharacterEntity()));
    }

    private void onCopyToClipboardClicked(UIWidget button) {
        getInteractionTarget().send(new StructureTemplateStringRequest(localPlayer.getCharacterEntity()));
    }

}
