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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.treeView.Tree;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.BlockFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureSpawnerFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureTemplateFromToolboxRequest;
import org.terasology.world.block.BlockExplorer;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.shapes.BlockShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Overlay shown when the user tries to spawn a building but can't because the preconditions are not met.
 */
public class ToolboxScreen extends BaseInteractionScreen {

    @In
    private ClipboardManager clipboardManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    private ToolboxTreeView treeView;


    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {

    }

    @Override
    public void initialise() {
        treeView = find("treeView", ToolboxTreeView.class);
        if (treeView != null) {
            Texture toolboxTexture = assetManager.getAsset("StructureTemplates:Toolbox16x16", Texture.class).get();
            ToolboxTree tree = new ToolboxTree(new ToolboxTreeValue("Toolbox", toolboxTexture, null));
            tree.addChild(createBlockSubTree());
            tree.addChild(createStructureSpawnersSubTree());
            tree.addChild(createStructureTemplatesSubTree());

            tree.setExpanded(true);

            treeView.setModel(tree);

            UIButton takeItemButton = find("getItemButton", UIButton.class);
            if (takeItemButton != null) {
                takeItemButton.subscribe(this::onTakeItemButton);
                takeItemButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                                               @Override
                                               public Boolean get() {
                                                   return getSelectedItemRequestFactory() != null;
                                               }
                                           }
                );
            }
        }

        UIButton closeButton = find("closeButton", UIButton.class);
        if (closeButton != null) {
            closeButton.subscribe(this::onCloseButton);
        }
    }

    private ToolboxTree createBlockSubTree() {
        Texture genericBlockTexture = assetManager.getAsset("StructureTemplates:Cube16x16Bright", Texture.class).get();

        ToolboxTree blockTree = new ToolboxTree(new ToolboxTreeValue("Blocks", genericBlockTexture, null));
        // hash set so that duplicates are eliminated
        Set<BlockUri> blocks = Sets.newHashSet();

        BlockExplorer blockExplorer = new BlockExplorer(assetManager);
        Iterables.addAll(blocks, blockManager.listRegisteredBlockUris());
        Iterables.addAll(blocks, blockExplorer.getAvailableBlockFamilies());
        Set<BlockUri> freeFormBlocks = blockExplorer.getFreeformBlockFamilies();
        Iterables.addAll(blocks, freeFormBlocks);

        List<BlockUri> blockList = Lists.newArrayList(blocks);
        blockList.sort((BlockUri o1, BlockUri o2) -> o1.toString().compareTo(o2.toString()));

        Set<ResourceUrn> blockShapes = assetManager.getAvailableAssets(BlockShape.class);


        for (BlockUri block : blockList) {
            if (!block.equals(BlockManager.AIR_ID) && !block.equals(BlockManager.UNLOADED_ID)) {
                /*
                 * Getting the block familiy here might cause some issues
                 * as blocks would be registered on the client. That is why we don;t do the out commented lines:
                 */
                // BlockFamily blockFamily = blockManager.getBlockFamily(block.getFamilyUri());
                // String displayName = blockFamily.getDisplayName();
                String displayName = block.toString();
                ToolboxTree blockFamiliyTree = new ToolboxTree(new ToolboxTreeValue(displayName, genericBlockTexture,
                        () -> new BlockFromToolboxRequest(block)));
                if (freeFormBlocks.contains(block)) {
                    for (ResourceUrn shareUrn: blockShapes) {

                        blockFamiliyTree.addChild(new ToolboxTreeValue(shareUrn.toString(),
                                genericBlockTexture,
                                () -> new BlockFromToolboxRequest(new BlockUri(block.getBlockFamilyDefinitionUrn(), shareUrn))));
                    }
                }
                blockTree.addChild(blockFamiliyTree);
            }
        }
        return blockTree;
    }

    private ToolboxTree createStructureSpawnersSubTree() {
        Optional<TextureRegionAsset> optionalTextureRegion = assetManager.getAsset("engine:items#whiteRecipe", TextureRegionAsset.class);
        TextureRegion texture = optionalTextureRegion.get();

        ToolboxTree structureTemplatesTree = new ToolboxTree(new ToolboxTreeValue("Structure Spawner", texture, null));


        Iterable<Prefab> prefabs = prefabManager.listPrefabs(StructureTemplateComponent.class);
        for (Prefab prefab: prefabs) {
            ToolboxTree item = new ToolboxTree(new ToolboxTreeValue(prefab.getUrn().toString(), texture,
                    () -> new StructureSpawnerFromToolboxRequest(prefab)));
            structureTemplatesTree.addChild(item);

        }
        return structureTemplatesTree;
    }

    private ToolboxTree createStructureTemplatesSubTree() {
        Optional<TextureRegionAsset> optionalTextureRegion = assetManager.getAsset("StructureTemplates:StructureTemplateOrigin", TextureRegionAsset.class);
        TextureRegion texture = optionalTextureRegion.get();

        ToolboxTree structureTemplatesTree = new ToolboxTree(new ToolboxTreeValue("Structure Templates", texture, null));


        Iterable<Prefab> prefabs = prefabManager.listPrefabs(StructureTemplateComponent.class);
        for (Prefab prefab: prefabs) {
            ToolboxTree item = new ToolboxTree(new ToolboxTreeValue(prefab.getUrn().toString(), texture,
                    () -> new StructureTemplateFromToolboxRequest(prefab)));
            structureTemplatesTree.addChild(item);

        }
        return structureTemplatesTree;
    }

    private void onTakeItemButton(UIWidget button) {
        Supplier<Event> itemRequestFactory = getSelectedItemRequestFactory();
        if (itemRequestFactory != null) {
            Event event = itemRequestFactory.get();
            getInteractionTarget().send(event);
        }
    }

    private Supplier<Event> getSelectedItemRequestFactory() {
        Integer selectedIndex = treeView.getSelectedIndex();
        if (selectedIndex == null) {
            return null;
        }
        Tree<ToolboxTreeValue> tree = treeView.getModel().getNode(selectedIndex);
        ToolboxTreeValue value = tree.getValue();
        return value.getItemRequestFactory();
    }

    private void onCloseButton(UIWidget button) {
        getManager().popScreen();
    }

}