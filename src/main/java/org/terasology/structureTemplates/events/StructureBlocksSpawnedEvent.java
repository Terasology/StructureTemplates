// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Gets send when the basic blocks of a structure template got placed. Event handlers typically add then entities that
 * rely on the blocks being present.
 */
public class StructureBlocksSpawnedEvent implements Event {
    private final BlockRegionTransform transformation;

    public StructureBlocksSpawnedEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
