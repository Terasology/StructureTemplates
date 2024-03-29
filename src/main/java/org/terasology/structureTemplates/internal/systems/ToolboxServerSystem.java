// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.SpawnTemplateActionComponent;
import org.terasology.structureTemplates.events.BlockFromToolboxRequest;
import org.terasology.structureTemplates.events.ItemFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureSpawnerFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureTemplateFromToolboxRequest;
import org.terasology.structureTemplates.internal.components.ToolboxComponent;
import org.terasology.structureTemplates.util.ItemType;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ToolboxServerSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledStructureSpawnSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;

    @In
    private AssetManager assetManager;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
    }

    @ReceiveEvent
    public void onBlockFromToolboxRequest(BlockFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();

        BlockFamily blockFamily = blockManager.getBlockFamily(event.getBlockUri());

        int amount;
        if (blockFamily.getArchetypeBlock().isStackable()) {
            amount = 99;
        } else {
            amount = 1;
        }
        EntityRef item = blockItemFactory.newInstance(blockFamily, amount);
        if (!item.exists()) {
            throw new IllegalArgumentException("Unknown block or item");
        }

        giveItemToOwnerOrDestroyItem(item, owner);

    }

    @ReceiveEvent(components = ToolboxComponent.class)
    public void onItemFromToolboxRequest(ItemFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();

        Prefab itemPrefab = event.getItemPrefab();
        EntityBuilder entityBuilder = entityManager.newBuilder(itemPrefab);
        ItemComponent itemComponent = entityBuilder.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            logger.error("Received request for {} which is not an item", itemPrefab);
            return;
        }
        EntityRef item = entityBuilder.build();
        giveItemToOwnerOrDestroyItem(item, owner);
    }

    @ReceiveEvent(components = ToolboxComponent.class)
    public void onStructureSpawnerFromToolboxRequest(StructureSpawnerFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();
        EntityRef item = createItem(event.getStructureTemplatePrefab(), ItemType.SPAWNER);
        giveItemToOwnerOrDestroyItem(item, owner);
    }

    @ReceiveEvent(components = ToolboxComponent.class)
    public void onStructureTemplateFromToolboxRequest(StructureTemplateFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();
        EntityRef item = createItem(event.getStructureTemplatePrefab(), ItemType.TEMPLATE);
        giveItemToOwnerOrDestroyItem(item, owner);
    }

    private void giveItemToOwnerOrDestroyItem(EntityRef item, EntityRef owner) {
        GiveItemEvent giveItemEvent = new GiveItemEvent(owner);
        item.send(giveItemEvent);
        if (!giveItemEvent.isHandled()) {
            item.destroy();
        }
    }

    /**
     * Create an item to for either a structure template or structure spawner based on the given prefab.
     *
     * @param prefab   the structure prefab to create an item for
     * @param itemType what kind of item to create
     * @return the created item entity
     */
    private EntityRef createItem(final Prefab prefab, final ItemType itemType) {
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        entityBuilder.addPrefab("engine:iconItem");

        ItemComponent itemComponent = getItemComponent(entityBuilder, itemType.iconUrn);
        if (itemType == ItemType.TEMPLATE) {
            itemComponent.consumedOnUse = true;
        }
        entityBuilder.addOrSaveComponent(itemComponent);

        DisplayNameComponent displayNameComponent =
                getDisplayNameComponent(entityBuilder, prefab, itemType.suffix);
        entityBuilder.addOrSaveComponent(displayNameComponent);

        switch (itemType) {
            case TEMPLATE:
                entityBuilder.addOrSaveComponent(new SpawnTemplateActionComponent());
                entityBuilder.removeComponent(SpawnStructureActionComponent.class);
                break;
            case SPAWNER:
                entityBuilder.addOrSaveComponent(new SpawnStructureActionComponent());
                break;
        }
        entityBuilder.addOrSaveComponent(new NetworkComponent());

        return entityBuilder.build();
    }

    /**
     * Create or modify a common item component for structure templates and structure spawners.
     *
     * @param entityBuilder entity builder to take an existing item component from, if present
     * @param iconUrn       the icon URN to be used for rendering the item
     * @return a modified item component if the entity builders has an item component, or a new component otherwise
     */
    private ItemComponent getItemComponent(final EntityBuilder entityBuilder, final String iconUrn) {
        final ItemComponent itemComponent =
                Optional.ofNullable(entityBuilder.getComponent(ItemComponent.class)).orElse(new ItemComponent());
        Optional<TextureRegionAsset> maybeTexture = assetManager.getAsset(iconUrn, TextureRegionAsset.class);
        itemComponent.icon = maybeTexture.orElse(null);
        itemComponent.damageType = assetManager.getAsset("engine:physicalDamage", Prefab.class).orElse(null);
        itemComponent.pickupPrefab = assetManager.getAsset("engine:itemPickup", Prefab.class).orElse(null);
        return itemComponent;
    }

    /**
     * Create or modify the display name component for structure templates and structure spawners.
     *
     * @param entityBuilder entity builder to take an existing display name component from, if present
     * @param prefab        the prefab to derive the display name from
     * @param suffix        the display name suffix to be displayed after the prefab name
     * @return a modified display name component if the entity builder has a display name component, or a new component otherwise
     */
    private DisplayNameComponent getDisplayNameComponent(final EntityBuilder entityBuilder, final Prefab prefab, final String suffix) {
        final DisplayNameComponent displayNameComponent =
                Optional.ofNullable(entityBuilder.getComponent(DisplayNameComponent.class))
                        .orElse(new DisplayNameComponent());
        displayNameComponent.name = prefab.getName() + suffix;

        return displayNameComponent;
    }

}
