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
package org.terasology.structureTemplates.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.world.block.family.BlockFamily;

import java.util.List;

/**
 * This component is intended to be used in structure templates.
 *
 * It adds items (incl. block items) to one ore more chests when the entity receives a
 * {@link SpawnStructureEvent}.
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
    }
}
