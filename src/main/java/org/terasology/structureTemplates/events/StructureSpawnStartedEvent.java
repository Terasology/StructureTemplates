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
