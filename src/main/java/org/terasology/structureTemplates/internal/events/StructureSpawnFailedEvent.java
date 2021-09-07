// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.structureTemplates.components.SpawnStructureActionComponent;

/**
 * Sent by the server to the owner of an item when a attempt to spawn a structure fails.
 *
 * The event is necessary as the server determines that the spawn is not possible and the client needs
 * to show a result.
 *
 * See {@link SpawnStructureActionComponent}.
 */
@OwnerEvent
public class StructureSpawnFailedEvent implements Event {
    private Prefab failedSpawnCondition;
    private BlockRegion spawnPreventingRegion;

    public StructureSpawnFailedEvent(Prefab failedSpawnCondition, BlockRegion spawnPreventingRegion) {
        this.failedSpawnCondition = failedSpawnCondition;
        this.spawnPreventingRegion = spawnPreventingRegion;
    }

    public StructureSpawnFailedEvent() {
        // for serialization
    }

    public Prefab getFailedSpawnCondition() {
        return failedSpawnCondition;
    }

    public BlockRegion getSpawnPreventingRegion() {
        return spawnPreventingRegion;
    }
}
