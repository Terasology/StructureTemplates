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

import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Send this event to a structure template entity to start the spawning of the structure.
 *
 * The consuming handler of this event determines how exactly the structure spawning will look like
 * (e.g. if it is instant or if blocks need time to appear). The standard handling of this event looks like this:
 *
 * On priority {@link EventPriority#PRIORITY_CRITICAL} there is a event handler that sends a
 * {@link StructureSpawnStartedEvent}. Implement a handler of {@link StructureSpawnStartedEvent} when you
 * want to add a component that does something when the spawning of a structure starts .(e.g. placing some spawn
 * particles or playing a placement sound)
 *
 * On priority {@link EventPriority#PRIORITY_TRIVIAL} a handler of this event will send a
 * {@link StructureBlocksSpawnedEvent}. If you want to introduce a component that makes
 * structure templates spawn entities you should subscribe to this event. At least
 * if it is not just a "spawning started" effect.
 */
public class SpawnStructureEvent extends AbstractConsumableEvent {
    private BlockRegionTransform transformation;

    public SpawnStructureEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
