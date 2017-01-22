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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.structureTemplates.internal.components.EditsCopyRegionComponent;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionRequest;
import org.terasology.structureTemplates.internal.events.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.internal.events.MakeBoxShapedRequest;
import org.terasology.structureTemplates.internal.ui.StructureTemplateRegionScreen;

/**
 * Main structure template editor UI
 */
public class StructureTemplateEditorScreen extends BaseInteractionScreen {
    private UIButton copyToClipboardButton;
    private UIButton createSpawnerButton;
    private UIButton copyGroundConditionButton;
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
        copyToClipboardButton = find("copyToClipboardButton", UIButton.class);
        if (copyToClipboardButton != null) {
            copyToClipboardButton.subscribe(this::onCopyToClipboardClicked);
        }

        createSpawnerButton = find("createSpawnerButton", UIButton.class);
        if (createSpawnerButton != null) {
            createSpawnerButton.subscribe(this::onCreateSpawnerButton);
        }

        copyGroundConditionButton = find("copyGroundConditionButton", UIButton.class);
        if (copyGroundConditionButton != null) {
            copyGroundConditionButton.subscribe(this::onCopyGroundConditionButton);
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

    private void onMakeBoxShapedButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
        Region3i region = component.editRegion;
        StructureTemplateRegionScreen regionScreen = getManager().pushScreen(
                "StructureTemplates:StructureTemplateRegionScreen", StructureTemplateRegionScreen.class);
        regionScreen.setRegion(region);
        regionScreen.setOkHandler((Region3i r) -> {
            getInteractionTarget().send(new MakeBoxShapedRequest(localPlayer.getCharacterEntity(), r));
        });
    }

    private void onCopyGroundConditionButton(UIWidget button) {
        EntityRef entity = getInteractionTarget();
        StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
        Region3i region = component.editRegion;
        clipboardManager.setClipboardContents(String.format(
                "{\"condition\": \"StructureTemplates:IsGroundLike\", \"region\" :{\"min\": [%d, %d, %d], \"size\": [%d, %d, %d]}}",
                region.minX(),region.minY(), region.minZ(),region.sizeX(), region.sizeY(), region.sizeZ()));
    }

    private void onCreateSpawnerButton(UIWidget button) {
        getInteractionTarget().send(new CreateStructureSpawnItemRequest(localPlayer.getCharacterEntity()));
    }

    private void onCopyToClipboardClicked(UIWidget button) {
        getInteractionTarget().send(new CopyBlockRegionRequest(localPlayer.getCharacterEntity()));
    }

    private abstract class AbstractIntEditorPropertyBinding implements Binding<String> {
        public abstract int getInt(StructureTemplateEditorComponent editorComponent);
        public abstract void setInt(StructureTemplateEditorComponent editorComponent,
                                    int value);
        @Override
        public String get() {
            EntityRef entity = getInteractionTarget();
            StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
            return Integer.toString(getInt(component));
        }

        @Override
        public void set(String value) {
            int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return; // do nothing (ignore invalid change request and keep old valid value)
            }
            EntityRef entity = getInteractionTarget();
            StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
            setInt(component ,intValue);
            entity.saveComponent(component);
        }
    }
}
