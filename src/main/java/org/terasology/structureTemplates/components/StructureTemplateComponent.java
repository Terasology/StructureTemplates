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
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.structureTemplates.util.AnimationType;

/**
 * All structure spawning entities should have this component.
 */
public class StructureTemplateComponent implements Component {
    /**
     * Prefab of the entitiy that represents the type of this structure spawner.
     */
    public Prefab type;

    /**
     * If this values is twice as large than that of another structure template then this structure template will twice
     * as often be picked when a random structure of the given type gets requested.
     */
    public int spawnChance = 100;

    public AnimationType animationType = AnimationType.LayerByLayer;

}
