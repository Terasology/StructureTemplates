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
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Send this event to a structure template entity to start the spawning of the structure.
 *
 * Each handler should do the following:
 *
 * <ul>
 *     <li>It must send at the start a {@link StructureSpawnStartedEvent}. Other event handlers might use it to trigger
 *     stuff. E.g. handlers of {@link StructureSpawnStartedEvent} might display soem particles or play a round
 *     when a certain component is present.</li>
 *     <li>It may place the blocks instantly or delayed. Possibly triggering a nice animation.
 *     To get the blocks to place it can send a {@link GetStructureTemplateBlocksEvent}.
 *     Alternativly it can also send a {@link SpawnBlocksOfStructureTemplateEvent} event to trigger
 *     the default block spawning</li>
 *     <li>After all blocks have been spawned it must send a {@link StructureBlocksSpawnedEvent}.
 *     The {@link StructureBlocksSpawnedEvent} triggers further structure finish up work like the placement of items
 *     in a chest.</li>
 *     <li>It must consume the event.</li>
 * </ul>
 *
 * If you don't want to add a new block placement animation then you should not implement a handler for this event.
 * If you want to create a component that triggers the spawning of for example some entities then you should
 * create a handler for {@link StructureBlocksSpawnedEvent} instead.
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
