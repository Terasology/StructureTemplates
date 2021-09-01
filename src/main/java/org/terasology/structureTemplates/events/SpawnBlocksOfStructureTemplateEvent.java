// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * This class is intended to be used by alternative structure spawning methods.
 *
 * It can be used to trigger the unconditional placement of blocks of a structure template.
 *
 */
public class SpawnBlocksOfStructureTemplateEvent implements Event {
    private BlockRegionTransform transformation;

    public SpawnBlocksOfStructureTemplateEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
