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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;

/**
 * All structure spawning entities should have this component.
 */
public class StructureTemplateComponent implements Component {
    /**
     * Describes on which side of the structure ( in global coordinate system) the front of the building is located.
     * locatin (The confusing thing is that the global orientation is not EAST, WEST, NORTH and SOUTH but also of
     * type Side....).
     */
    public Side front;
    /**
     * Describes where the spawn locatin is in the template coordinate system.
     *
     * e.g. the block that is at this position in the template will be placed at at the selected spawn location
     */
    public Vector3i spawnPosition = new Vector3i(0, 0, 0);
    /**
     * Prefab of the entitiy that represents the type of this structure spawner.
     */
    public Prefab type;

}
