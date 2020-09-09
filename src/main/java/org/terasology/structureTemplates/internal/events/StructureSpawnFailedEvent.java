// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;

/**
 * Sent by the server to the owner of an item when a attempt to spawn a structure fails.
 * <p>
 * The event is necessary as the server determines that the spawn is not possible and the client needs to show a
 * result.
 * <p>
 * See {@link SpawnStructureActionComponent}.
 */
@OwnerEvent
public class StructureSpawnFailedEvent implements Event {
    private Prefab failedSpawnCondition;
    private Region3i spawnPreventingRegion;

    public StructureSpawnFailedEvent(Prefab failedSpawnCondition, Region3i spawnPreventingRegion) {
        this.failedSpawnCondition = failedSpawnCondition;
        this.spawnPreventingRegion = spawnPreventingRegion;
    }

    public StructureSpawnFailedEvent() {
        // for serialization
    }

    public Prefab getFailedSpawnCondition() {
        return failedSpawnCondition;
    }

    public Region3i getSpawnPreventingRegion() {
        return spawnPreventingRegion;
    }
}
