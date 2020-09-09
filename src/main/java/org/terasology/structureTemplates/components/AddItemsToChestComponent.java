// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

import java.util.List;

/**
 * This component is intended to be used in structure templates.
 * <p>
 * It adds items (incl. block items) to one ore more chests when the entity receives a {@link SpawnStructureEvent}.
 */
public class AddItemsToChestComponent implements Component {
    public List<ChestToFill> chestsToFill;

    @MappedContainer
    public static class ChestToFill {
        /**
         * Position of the chest to be filled
         */
        public Vector3i position;
        public List<Item> items;
    }

    /**
     * Either {@link #itemPrefab} or {@link #blockFamiliy} needs to be set, but not both.
     * <p>
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
    }
}
