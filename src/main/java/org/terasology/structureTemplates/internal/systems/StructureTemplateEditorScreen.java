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
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.structureTemplates.internal.components.CreateStructureSpawnItemRequest;
import org.terasology.structureTemplates.internal.events.CopyBlockRegionRequest;

/**
 * Main structure template editor UI
 */
public class StructureTemplateEditorScreen extends BaseInteractionScreen {

    private UIText minXField;
    private UIText minYField;
    private UIText minZField;
    private UIText maxXField;
    private UIText maxYField;
    private UIText maxZField;
    private UIButton copyToClipboardButton;
    private UIButton createSpawnerButton;
    private UIButton copyGroundConditionButton;

    @In
    private ClipboardManager clipboardManager;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        // nothing to do
    }

    @Override
    public void initialise() {
        minXField = find("minXField", UIText.class);
        minXField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.minX();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = new Vector3i(value, region.minY(), region.minZ());
                Vector3i max = region.max();
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

        minYField = find("minYField", UIText.class);
        minYField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.minY();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = new Vector3i( region.minX(), value, region.minZ());
                Vector3i max = region.max();
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

        minZField = find("minZField", UIText.class);
        minZField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.minZ();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = new Vector3i( region.minX(), region.minY(), value);
                Vector3i max = region.max();
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

        maxXField = find("maxXField", UIText.class);
        maxXField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.maxX();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = region.min();
                Vector3i max = new Vector3i(value, region.maxY(), region.maxZ());
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

        maxYField = find("maxYField", UIText.class);
        maxYField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.maxY();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = region.min();
                Vector3i max = new Vector3i(region.maxX(), value, region.maxZ());
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

        maxZField = find("maxZField", UIText.class);
        maxZField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.maxZ();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                Region3i region = editorComponent.editRegion;
                Vector3i min = region.min();
                Vector3i max = new Vector3i(region.maxX(), region.maxY(), value);
                editorComponent.editRegion = Region3i.createBounded(min, max);
            }
        });

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
        getInteractionTarget().send(new CreateStructureSpawnItemRequest());
    }

    private void onCopyToClipboardClicked(UIWidget button) {
        getInteractionTarget().send(new CopyBlockRegionRequest());
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
