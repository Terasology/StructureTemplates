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
package org.terasology.structureTemplates.events;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.HashMap;
import java.util.Map;

/**
 * Send this event to a structure template entity to determine the blocks that should be placed.
 *
 * Based on the components of a structure template, systems will fill this map.
 *
 * The blocks in the map have the transformation applied.
 */
public class GetStructureTemplateBlocksEvent implements Event {
    private BlockRegionTransform transformation;
    /**
     * Final placement position to final block type map: The transformation is already applied.
     * The position is the coordinate of the block in the world.
     */
    private Map<Vector3i, Block> blocksToPlace = new HashMap<>();

    public GetStructureTemplateBlocksEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }

    public Map<Vector3i, Block> getBlocksToPlace() {
        return blocksToPlace;
    }

    public void fillRegion(BlockRegionc region, Block block) {
        for (Vector3ic pos : region) {
            blocksToPlace.put(new Vector3i(pos), block);
        }
    }
}
