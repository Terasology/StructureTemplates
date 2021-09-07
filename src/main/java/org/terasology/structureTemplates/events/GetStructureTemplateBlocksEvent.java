// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.gestalt.entitysystem.event.Event;
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
