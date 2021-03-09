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
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * This class is intended to be used by alternative structure spawning methods.
 *
 * It can be used to trigger the unconditional placement of blocks of a structure template.
 *
 */
public class SpawnBlocksOfStructureTemplateEvent implements Event {
    private BlockRegionTransform transformation;

    public SpawnBlocksOfStructureTemplateEvent(BlockRegionTransform transform) {
        this.transformation = transform;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }
}
