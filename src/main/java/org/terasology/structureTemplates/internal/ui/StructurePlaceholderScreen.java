// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIText;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;
import org.terasology.structureTemplates.internal.components.StructurePlaceholderComponent;
import org.terasology.structureTemplates.internal.events.RequestStructurePlaceholderPrefabSelection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog for setting a region
 */
public class StructurePlaceholderScreen extends BaseInteractionScreen {

    private UIDropdown<Prefab> comboBox;
    private UIButton closeButton;

    private Prefab selectedPrefab;

    @In
    private PrefabManager prefabManager;

    @In
    private LocalPlayer localPlayer;


    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        selectedPrefab = null;
        StructurePlaceholderComponent comp = interactionTarget.getComponent(StructurePlaceholderComponent.class);
        selectedPrefab = comp.selectedPrefab;


    }

    @Override
    public void initialise() {
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
                    localPlayer.getCharacterEntity().send(new RequestStructurePlaceholderPrefabSelection(selectedPrefab));
                }
            });
        }
        UIText fullDescriptionLabel = find("fullDescriptionLabel", UIText.class);
        fullDescriptionLabel.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (selectedPrefab == null) {
                    return "";
                }
                DisplayNameComponent displayNameComponent = selectedPrefab.getComponent(DisplayNameComponent.class);
                if (displayNameComponent == null) {
                    return "";
                }
                return displayNameComponent.description;
            }
        });

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

}
