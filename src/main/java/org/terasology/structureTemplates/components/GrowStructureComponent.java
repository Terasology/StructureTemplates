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
package org.terasology.structureTemplates.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

/**
 * Rises a block structure from the ground up when an entity with this component receives a {@link SpawnStructureEvent} and it also has a {@link SpawnBlockRegionsComponent}.
 */
public class GrowStructureComponent implements Component {
}
