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
package org.terasology.structureTemplates.internal.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.Region3i;
import org.terasology.network.OwnerEvent;
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
