// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Send this event to a structure template entity to make it spawn the template in edit mode.
 * <p>
 * Send {@link SpawnStructureEvent} if you want to spawn the structure of the template regularly.
 */
public class SpawnTemplateEvent implements Event {
    private final BlockRegionTransform transformation;

    public SpawnTemplateEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
