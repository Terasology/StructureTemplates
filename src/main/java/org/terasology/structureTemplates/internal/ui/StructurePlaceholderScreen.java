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
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;
import org.terasology.structureTemplates.internal.components.StructurePlaceholderComponent;
import org.terasology.structureTemplates.internal.events.RequestPrefabSelection;

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
                    getInteractionTarget().send(new RequestPrefabSelection(selectedPrefab));
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
                return displayNameComponent.description;            }
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
