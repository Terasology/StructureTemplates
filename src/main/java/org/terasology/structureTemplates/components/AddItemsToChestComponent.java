// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This component is intended to be used in structure templates.
 *
 * It adds items (incl. block items) to one ore more chests when the entity receives a
 * {@link SpawnStructureEvent}.
 */
public class AddItemsToChestComponent implements Component<AddItemsToChestComponent> {
    public List<ChestToFill> chestsToFill;

    @Override
    public void copy(AddItemsToChestComponent other) {
        this.chestsToFill = other.chestsToFill.stream()
                .map(ChestToFill::copy)
                .collect(Collectors.toList());
    }

    @MappedContainer
    public static class ChestToFill {
        /**
         * Position of the chest to be filled
         */
        public Vector3i position;
        public List<Item> items;

        ChestToFill copy() {
            ChestToFill newChestToFill = new ChestToFill();
            newChestToFill.position = new Vector3i(this.position);
            newChestToFill.items = this.items.stream()
                    .map(Item::copy)
                    .collect(Collectors.toList());
            return newChestToFill;
        }
    }

    /**
     * Either {@link #itemPrefab} or {@link #blockFamiliy} needs to be set, but not both.
     *
     * The field {@link #amount} gets only used if {@link #blockFamiliy} != null.
     */
    @MappedContainer
    public static class Item {
        /**
         * Optional field: Inventory slot in which the item should be placed
         */
        public Integer slot;
        /**
         * Prefab of the item to spawn
         */
        public Prefab itemPrefab;
        /**
         * The block familiy
         */
        public BlockFamily blockFamiliy;
        /**
         * The field {@link #amount} gets only used if {@link #blockFamiliy} != null.
         */
        public int amount = 1;

        Item copy() {
            Item newItem = new Item();
            newItem.slot = this.slot;
            newItem.itemPrefab = this.itemPrefab;
            newItem.blockFamiliy = this.blockFamiliy;
            newItem.amount = this.amount;
            return newItem;
        }
    }
}
