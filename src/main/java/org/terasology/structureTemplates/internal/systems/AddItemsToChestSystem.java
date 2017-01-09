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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.AddItemsToChestComponent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.items.BlockItemFactory;

/**
 * System to power the {@link AddItemsToChestComponent}
 * Adds items to chests specified by the AddItemsToChestComponent.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AddItemsToChestSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledStructureSpawnSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    /**
     * Listens to structures spawning. When a structure spawns, all of chests specified in the prefab get filled with the specified items through @link addItemToChest.
     * @param event The event passed.
     * @param component The component as provided in the prefab. Contains the items, location and such.
     * @param entity A wrapper around an entity id providing access to common functionality.
     */
    @ReceiveEvent
    public void onSpawnStructureEvent(StructureBlocksSpawnedEvent event, EntityRef entity,
                                      AddItemsToChestComponent component) {
        BlockRegionTransform transformation = event.getTransformation();

        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        for (AddItemsToChestComponent.ChestToFill chestToFill: component.chestsToFill) {
            Vector3i absolutePosition = transformation.transformVector3i(chestToFill.position);
            EntityRef chest = blockEntityRegistry.getBlockEntityAt(absolutePosition);
            for (AddItemsToChestComponent.Item item: chestToFill.items) {
                addItemToChest(chest, item, blockFactory);
            }
        }
    }

    /**
     * A private method used by @link onSpawnStructureEvent. It actually adds the items to the chest.
     * @param chest The chest to add the items to.
     * @param item The item to be added.
     * @param blockFactory If this item is a block, this parameter is used to make a new instance of the block as an item.
     */
    private void addItemToChest(EntityRef chest, AddItemsToChestComponent.Item item, BlockItemFactory blockFactory) {
        EntityRef itemEntity;
        if (item.itemPrefab != null) {
            itemEntity = entityManager.create(item.itemPrefab);
        } else if (item.blockFamiliy != null) {
            itemEntity = blockFactory.newInstance(item.blockFamiliy, item.amount);
        } else {
            logger.warn("Can't add item to chest as neither blockFamily nor itemPrefab has been defined");
            return;
        }
        if (item.slot != null) {
            inventoryManager.giveItem(chest, EntityRef.NULL, itemEntity, item.slot);
        } else {
            inventoryManager.giveItem(chest, EntityRef.NULL, itemEntity);
        }
    }
}
