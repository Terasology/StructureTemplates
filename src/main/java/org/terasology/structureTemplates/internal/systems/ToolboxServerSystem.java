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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;
import org.terasology.structureTemplates.components.SpawnTemplateActionComponent;
import org.terasology.structureTemplates.events.BlockFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureSpawnerFromToolboxRequest;
import org.terasology.structureTemplates.events.StructureTemplateFromToolboxRequest;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Optional;

/**
 *
 */
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
    public void onRBlockFromToolboxRequest(BlockFromToolboxRequest event, EntityRef toolboxEntity) {
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

    void giveItemToOwnerOrDestroyItem(EntityRef item, EntityRef owner) {
        GiveItemEvent giveItemEvent = new GiveItemEvent(owner);
        item.send(giveItemEvent);
        if (!giveItemEvent.isHandled()) {
            item.destroy();
        }
    }

    @ReceiveEvent
    public void onStructureSpawnerFromToolboxRequest(StructureSpawnerFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();

        EntityBuilder entityBuilder = entityManager.newBuilder(event.getStructureTemplatePrefab());
        ItemComponent itemComponent = entityBuilder.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            itemComponent = new ItemComponent();
        }
        Optional<TextureRegionAsset> optionalIcon = assetManager.getAsset("engine:items#whiteRecipe", TextureRegionAsset.class);
        itemComponent.icon = optionalIcon.get();
        itemComponent.damageType = assetManager.getAsset("engine:physicalDamage", Prefab.class).get();
        itemComponent.pickupPrefab = assetManager.getAsset("engine:itemPickup", Prefab.class).get();
        entityBuilder.addOrSaveComponent(itemComponent);
        DisplayNameComponent displayNameComponent = entityBuilder.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            displayNameComponent = new DisplayNameComponent();
        }

        displayNameComponent.name =  event.getStructureTemplatePrefab().getName() + " Spawner";
        entityBuilder.addOrSaveComponent(displayNameComponent);
        entityBuilder.addOrSaveComponent(new SpawnStructureActionComponent());
        entityBuilder.addOrSaveComponent(new NetworkComponent());
        EntityRef item = entityBuilder.build();
        giveItemToOwnerOrDestroyItem(item, owner);

    }


    @ReceiveEvent
    public void onStructureTemplateFromToolboxRequest(StructureTemplateFromToolboxRequest event, EntityRef toolboxEntity) {
        EntityRef owner = toolboxEntity.getOwner();

        EntityBuilder entityBuilder = entityManager.newBuilder(event.getStructureTemplatePrefab());
        ItemComponent itemComponent = entityBuilder.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            itemComponent = new ItemComponent();
        }
        Optional<TextureRegionAsset> optionalIcon = assetManager.getAsset("StructureTemplates:StructureTemplateEditor", TextureRegionAsset.class);
        itemComponent.icon = optionalIcon.get();
        itemComponent.damageType = assetManager.getAsset("engine:physicalDamage", Prefab.class).get();
        itemComponent.pickupPrefab = assetManager.getAsset("engine:itemPickup", Prefab.class).get();
        entityBuilder.addOrSaveComponent(itemComponent);
        DisplayNameComponent displayNameComponent = entityBuilder.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            displayNameComponent = new DisplayNameComponent();
        }

        displayNameComponent.name =  event.getStructureTemplatePrefab().getName() + " Template";
        entityBuilder.addOrSaveComponent(displayNameComponent);
        entityBuilder.addOrSaveComponent(new SpawnTemplateActionComponent());
        entityBuilder.removeComponent(SpawnStructureActionComponent.class);
        entityBuilder.addOrSaveComponent(new NetworkComponent());
        EntityRef item = entityBuilder.build();
        giveItemToOwnerOrDestroyItem(item, owner);

    }

}
