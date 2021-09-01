// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.engine.world.block.BlockExplorer;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.structureTemplates.internal.components.ReplaceWallItemComponent;
import org.terasology.structureTemplates.internal.events.ReplaceBlocksRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Screen for the wall replacer item.
 */
public class ReplaceWallScreen extends BaseInteractionScreen {

    private UIDropdownScrollable<BlockUri> blockComboBox;
    private UIDropdown<ReplaceWallItemComponent.ReplacementType> replacementTypeComboBox;
    private UIButton cancelButton;
    private UIButton placeWallButton;

    private Prefab selectedPrefab;

    @In
    private PrefabManager prefabManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {

    }

    @Override
    public void initialise() {
        blockComboBox = find("blockComboBox", UIDropdownScrollable.class);
        BlockExplorer blockExplorer = new BlockExplorer(assetManager);
        Set<BlockUri> blocks = blockExplorer.getFreeformBlockFamilies();
        blocks.add(new BlockUri("CoreAssets:Water"));
        blocks.add(new BlockUri("CoreAssets:Lava"));
        blocks.add(BlockManager.AIR_ID);
        List<BlockUri> blockList = Lists.newArrayList(blocks);
        blockList.sort((BlockUri o1, BlockUri o2) -> o1.toString().compareTo(o2.toString()));
        blockComboBox.setOptions(blockList);
        blockComboBox.bindSelection(new Binding<BlockUri>() {
            @Override
            public BlockUri get() {
                return new BlockUri(getInteractionTarget().getComponent(ReplaceWallItemComponent.class).blockUri);
            }

            @Override
            public void set(BlockUri value) {
                EntityRef entity = getInteractionTarget();
                ReplaceWallItemComponent component = entity.getComponent(ReplaceWallItemComponent.class);
                component.blockUri = value.toString();
                entity.saveComponent(component);
            }
        });

        cancelButton = find("cancelButton", UIButton.class);
        if (cancelButton != null) {
            cancelButton.subscribe(this::onCloseButton);
        }

        placeWallButton = find("placeWallButton", UIButton.class);
        if (placeWallButton != null) {
            placeWallButton.subscribe(this::onPlaceWallButton);
        }



        replacementTypeComboBox = find("replacementTypeComboBox", UIDropdown.class);
        if (replacementTypeComboBox != null) {
            replacementTypeComboBox.setOptions(Arrays.asList(ReplaceWallItemComponent.ReplacementType.values()));
            replacementTypeComboBox.bindSelection(new Binding<ReplaceWallItemComponent.ReplacementType>() {
                @Override
                public ReplaceWallItemComponent.ReplacementType get() {
                    return getInteractionTarget().getComponent(ReplaceWallItemComponent.class).replacementType;
                }

                @Override
                public void set(ReplaceWallItemComponent.ReplacementType value) {
                    EntityRef entity = getInteractionTarget();
                    ReplaceWallItemComponent component = entity.getComponent(ReplaceWallItemComponent.class);
                    component.replacementType = value;
                    entity.saveComponent(component);
                }
            });
        }

    }

    private void onPlaceWallButton(UIWidget button) {
        getInteractionTarget().send(new ReplaceBlocksRequest());
        getManager().popScreen();
    }

    private void onCloseButton(UIWidget button) {
        getManager().popScreen();
    }


}
