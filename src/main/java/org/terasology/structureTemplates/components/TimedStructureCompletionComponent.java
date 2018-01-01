/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.structureTemplates.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
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
public class TimedStructureCompletionComponent implements Component {
    public EntityRef structureTemplate;
    public long gameTimeInMsWhenStructureGetsCompleted;
}
