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
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;

/**
 * Send this event to a structure template entity to start the spawning of the structure.
 *
 * On priority {@link EventPriority#PRIORITY_CRITICAL} there is a event handler that sends a
 * {@link StructureSpawnStartedEvent}. Implement a handler of {@link StructureSpawnStartedEvent} when you
 * want to add a component that does something when the spawning of a structure starts .(e.g. placing some spawn
 * particles or playing a placement sound)
 *
 * On priority {@link EventPriority#PRIORITY_TRIVIAL} a handler of this event will send a
 * {@link StructureBlocksSpawnedEvent}.
 *
 * If you want to introduce a component that makes structure templates spawn entities you should
 * subscribe to this event. At least if it is not just a "spawning started" effect.
 *
 * Please note: the usage of this component will be changed, please do not add systtems that subscribe for this event.
 *
 * In future it will made a consumable event, and it will be up to the implementor of the consumable event
 * when it will send the  {@link StructureBlocksSpawnedEvent} event. e.g. a system might place the blocks
 * over a larger time frame and only when it is done the entities will be placed.
 */
public class SpawnStructureEvent implements Event {
    private BlockRegionTransform transformation;

    public SpawnStructureEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
