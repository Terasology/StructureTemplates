// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * The event gets sent to a structure template entity when a structure template spawning process started.
 *
 * Implement a handler of {@link StructureSpawnStartedEvent} when you
 * want to add a structure template component that does something when the spawning of a structure starts .
 * (e.g. placing some spawn particles or playing a placement sound)
 */
public class StructureSpawnStartedEvent implements Event {
    private BlockRegionTransform transformation;

    public StructureSpawnStartedEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
