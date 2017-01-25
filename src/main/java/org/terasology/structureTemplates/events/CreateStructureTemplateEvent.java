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

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This event gets sent to the structure template editor entity (the origin block) when
 * a new structure template should be created from blocks that have been marked by that structure template editor.
 */
public class CreateStructureTemplateEvent implements Event {
    private BlockRegionTransform absoluteToRelativeTransform;
    /**
     * Describes the blocks that should be added to the structure template.
     * Event handlers can modify this map if they want for example to have a certain block type replaced by another.
     */
    private Map<Block, Set<Vector3i>> blocksToPlace = new HashMap<>();

    public CreateStructureTemplateEvent(BlockRegionTransform transform) {
        this.absoluteToRelativeTransform = transform;
    }

    public BlockRegionTransform getAbsoluteToRelativeTransform() {
        return absoluteToRelativeTransform;
    }

    public Map<Block, Set<Vector3i>> getBlocksToPlace() {
        return blocksToPlace;
    }
}
