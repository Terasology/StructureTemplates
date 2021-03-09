// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.clipboard.ClipboardManager;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.engine.world.block.BlockExplorer;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.treeView.Tree;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.events.BlockFromToolboxRequest;
import org.terasology.structureTemplates.events.ItemFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureSpawnerFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureTemplateFromToolboxRequest;
import org.terasology.structureTemplates.util.ItemType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    @In
    private WorldAtlas worldAtlas;

    private ToolboxTreeView treeView;

    private Texture genericBlockTexture;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {

    }

    @Override
    public void initialise() {
        genericBlockTexture = assetManager.getAsset("StructureTemplates:Cube16x16Bright", Texture.class).get();
        treeView = find("treeView", ToolboxTreeView.class);
        if (treeView != null) {
            Texture toolboxTexture = assetManager.getAsset("StructureTemplates:Toolbox16x16", Texture.class).get();
            ToolboxTree tree = new ToolboxTree(new ToolboxTreeValue("Toolbox", toolboxTexture, null));
            tree.addChild(createBlockSubTree());
            tree.addChild(createStructureTemplatesTools());
            tree.addChild(createSubTree(ItemType.SPAWNER, StructureSpawnerFromToolboxRequest::new));
            tree.addChild(createSubTree(ItemType.TEMPLATE, StructureTemplateFromToolboxRequest::new));

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

        ToolboxTree blockTree = new ToolboxTree(new ToolboxTreeValue("Blocks", genericBlockTexture, null));
        // hash set so that duplicates are eliminated
        Set<BlockUri> blocks = Sets.newHashSet();

        BlockExplorer blockExplorer = new BlockExplorer(assetManager);
        Iterables.addAll(blocks, blockManager.listRegisteredBlockUris());
        Iterables.addAll(blocks, blockExplorer.getAvailableBlockFamilies());
        Set<BlockUri> freeFormBlocks = blockExplorer.getFreeformBlockFamilies();
        Iterables.addAll(blocks, freeFormBlocks);

        List<BlockUri> blockList = Lists.newArrayList(blocks);
        blockList.sort(Comparator.comparing(BlockUri::toString));

        Set<ResourceUrn> blockShapes = assetManager.getAvailableAssets(BlockShape.class);


        for (BlockUri block : blockList) {
            if (!block.equals(BlockManager.AIR_ID) && !block.equals(BlockManager.UNLOADED_ID)) {
                /*
                 * Getting the block family here might cause some issues
                 * as blocks would be registered on the client. That is why we don't do the out commented lines:
                 */
                // BlockFamily blockFamily = blockManager.getBlockFamily(block.getFamilyUri());
                // String displayName = blockFamily.getDisplayName();
                ToolboxTree blockFamiliyTree = createBlockNode(block);
                if (freeFormBlocks.contains(block)) {
                    for (ResourceUrn shareUrn : blockShapes) {

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

    private ToolboxTree createBlockNode(BlockUri block) {
        String displayName = block.toString();
        return new ToolboxTree(new ToolboxTreeValue(displayName, genericBlockTexture,
                () -> new BlockFromToolboxRequest(block)));
    }

    private Prefab getPrefab(String prefab) {
        return assetManager.getAsset(prefab, Prefab.class)
                .orElseThrow(() -> new RuntimeException("Can't find prefab:" + prefab));
    }

    private ToolboxTree createStructureTemplatesTools() {
        Optional<TextureRegionAsset> optionalTextureRegion =
                assetManager.getAsset("StructureTemplates:StructureTemplateGenerator", TextureRegionAsset.class);
        TextureRegion texture = optionalTextureRegion.orElse(null);

        ToolboxTree tree =
                new ToolboxTree(new ToolboxTreeValue("Structure Template Tools", texture, null));

        tree.addChild(createBlockNode(new BlockUri("StructureTemplates:StructureTemplateOrigin")));
        tree.addChild(createItemNode(getPrefab("StructureTemplates:StructureTemplateGenerator")));
        tree.addChild(createBlockNode(new BlockUri("StructureTemplates:StructurePlaceholder")));
        tree.addChild(createItemNodeWithIcon(getPrefab("StructureTemplates:WallReplacer"), "StructureTemplates:WallReplacer16x16"));

        return tree;
    }

    private ToolboxTree createItemNodeWithIcon(Prefab itemPrefab, String iconUrn) {
        TextureRegionAsset<?> icon = assetManager.getAsset(iconUrn, Texture.class).get();
        String text = itemPrefab.getUrn().toString();
        return new ToolboxTree(new ToolboxTreeValue(text, icon,
                () -> new ItemFromToolboxRequest(itemPrefab)));
    }

    private ToolboxTree createItemNode(Prefab itemPrefab) {
        ItemComponent itemComponent = itemPrefab.getComponent(ItemComponent.class);
        TextureRegionAsset<?> icon = itemComponent.icon;
        String text = itemPrefab.getUrn().toString();
        return new ToolboxTree(new ToolboxTreeValue(text, icon,
                () -> new ItemFromToolboxRequest(itemPrefab)));
    }

    private ToolboxTree createSubTree(final ItemType itemType, final Function<Prefab, Event> itemRequest) {

        Optional<TextureRegionAsset> optionalTextureRegion = assetManager.getAsset(itemType.thumbnailUrn, TextureRegionAsset.class);
        TextureRegion texture = optionalTextureRegion.orElse(null);

        ToolboxTree subTree = new ToolboxTree(new ToolboxTreeValue("Structure" + itemType.suffix, texture, null));

        Prefab structureTemplateOriginPrefab = assetManager.getAsset("StructureTemplates:StructureTemplateOrigin", Prefab.class).get();
        List<Prefab> prefabs = prefabManager.listPrefabs(StructureTemplateComponent.class).stream()
                .filter(prefab -> prefab != structureTemplateOriginPrefab)
                .sorted(Comparator.comparing(o -> o.getUrn().toString()))
                .collect(Collectors.toList());

        for (Prefab prefab : prefabs) {
            ToolboxTree item = new ToolboxTree(new ToolboxTreeValue(prefab.getUrn().toString(), texture,
                    () -> itemRequest.apply(prefab)));
            subTree.addChild(item);
        }

        return subTree;
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
