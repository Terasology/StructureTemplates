// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;

/**
 * This component marks a entity to be a placeholder for a structure template spawning in progress for which
 * at a specific game time a structure will be spawned.
 *
 * A entity with this component should also have a {@link BlockRegionTransformComponent} that specifices at
 * which location and rotation the structure shall be spawned.
 *
 * When the specified game time has reached a {@link StructureBlocksSpawnedEvent} gets send to the structure template
 * entity to trigger the spawning of non block parts of the structure like items in a chest.
 */
public class TimedStructureCompletionComponent implements Component<TimedStructureCompletionComponent> {
    public EntityRef structureTemplate;
    public long gameTimeInMsWhenStructureGetsCompleted;

    @Override
    public void copy(TimedStructureCompletionComponent other) {
        this.structureTemplate = other.structureTemplate;
        this.gameTimeInMsWhenStructureGetsCompleted = other.gameTimeInMsWhenStructureGetsCompleted;
    }
}
