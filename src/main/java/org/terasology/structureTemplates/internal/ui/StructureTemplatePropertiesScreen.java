// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIText;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;
import org.terasology.structureTemplates.internal.events.RequestStructureTemplatePropertiesChange;
import org.terasology.structureTemplates.util.AnimationType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog for setting a region
 */
public class StructureTemplatePropertiesScreen extends CoreScreenLayer {

    private UIDropdown<Prefab> comboBox;
    private UIDropdown<AnimationType> comboBoxAnimation;
    private UIText spawnChanceTextBox;
    private UIButton closeButton;

    private String spawnChanceString;
    private Prefab selectedPrefab;
    private AnimationType animationType;

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
            List<AnimationType> animations = new ArrayList<>();
            animations.add(AnimationType.LayerByLayer);
            animations.add(AnimationType.FallingBlock);
            animations.add(AnimationType.NoAnimation);
            comboBoxAnimation.setOptions(animations);
            comboBoxAnimation.setOptionRenderer(new StringTextRenderer<AnimationType>() {
                @Override
                public String getString(AnimationType value) {
                    switch (value) {
                        case NoAnimation:
                            return "No animation";

                        case FallingBlock:
                            return "Falling Block";

                        default:
                            return "Layer-by-Layer";
                    }
                }
            });
            comboBoxAnimation.bindSelection(new Binding<AnimationType>() {
                @Override
                public AnimationType get() {
                    return animationType;
                }

                @Override
                public void set(AnimationType value) {
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
