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
package org.terasology.structureTemplates.internal.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;
import org.terasology.structureTemplates.internal.events.RequestStructureTemplatePropertiesChange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog for setting a region
 */
public class StructureTemplatePropertiesScreen extends CoreScreenLayer {

    private UIDropdown<Prefab> comboBox;
    private UIDropdown<String> comboBoxAnimation;
    private UIText spawnChanceTextBox;
    private UIButton closeButton;

    private String spawnChanceString;
    private Prefab selectedPrefab;
    private String animationType;

    private EntityRef editorEntity;

    @In
    private PrefabManager prefabManager;

    @In
    private LocalPlayer localPlayer;

    public EntityRef getEditorEntity() {
        return editorEntity;
    }

    public void setEditorEntity(EntityRef editorEntity) {
        this.editorEntity = editorEntity;
        selectedPrefab = null;
        StructureTemplateComponent comp = editorEntity.getComponent(StructureTemplateComponent.class);
        selectedPrefab = comp.type;
        spawnChanceString = Integer.toString(comp.spawnChance);
        animationType = comp.animationType;
    }


    @Override
    public void initialise() {
        spawnChanceTextBox = find("spawnChanceTextBox", UIText.class);
        if (spawnChanceTextBox != null) {
            spawnChanceTextBox.bindText(new Binding<String>() {
                @Override
                public String get() {
                    return spawnChanceString;
                }

                @Override
                public void set(String value) {
                    spawnChanceString = value;
                    requestServerToTakeOverCurrentValues();
                }
            });
        }
        comboBox = find("comboBox", UIDropdown.class);
        if (comboBox != null) {
            Iterable<Prefab> prefabIterable = prefabManager.listPrefabs(StructureTemplateTypeComponent.class);
            List<Prefab> prefabs = new ArrayList<>();
            for (Prefab prefab : prefabIterable) {
                prefabs.add(prefab);
            }
            prefabs.sort(Comparator.comparing(Prefab::getName));
            comboBox.setOptions(prefabs);
            comboBox.setOptionRenderer(new StringTextRenderer<Prefab>() {
                @Override
                public String getString(Prefab value) {
                    DisplayNameComponent displayNameComponent = value.getComponent(DisplayNameComponent.class);
                    if (displayNameComponent == null) {
                        return value.getUrn().toString();
                    }
                    return displayNameComponent.name;
                }
            });
            comboBox.bindSelection(new Binding<Prefab>() {
                @Override
                public Prefab get() {
                    return selectedPrefab;
                }

                @Override
                public void set(Prefab value) {
                    selectedPrefab = value;
                    requestServerToTakeOverCurrentValues();
                }
            });
        }

        comboBoxAnimation = find("comboBoxAnimation", UIDropdown.class);
        if (comboBoxAnimation != null) {
            List<String> animations = new ArrayList<>();
            animations.add("Layer-by-Layer");
            animations.add("Falling Block");
            animations.add("No animation");
            comboBoxAnimation.setOptions(animations);

            comboBoxAnimation.bindSelection(new Binding<String>() {
                @Override
                public String get() {
                    return animationType;
                }

                @Override
                public void set(String value) {
                    animationType = value;
                    requestServerToTakeOverCurrentValues();
                }
            });
        }

        closeButton = find("closeButton", UIButton.class);
        if (closeButton != null) {
            closeButton.subscribe(this::onCloseButton);
        }

    }

    private void onOkButton(UIWidget button) {
        getManager().popScreen();
    }

    private void onCloseButton(UIWidget button) {
        getManager().popScreen();
    }

    private void requestServerToTakeOverCurrentValues() {
        Integer spawnChance = null;
        try {
            spawnChance = Integer.parseInt(spawnChanceString);
        } catch (NumberFormatException e) {
            spawnChance = 0;
        }
        localPlayer.getCharacterEntity().send(new RequestStructureTemplatePropertiesChange(selectedPrefab, spawnChance, animationType));

    }
}
