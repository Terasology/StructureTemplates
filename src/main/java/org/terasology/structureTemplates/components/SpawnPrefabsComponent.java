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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;

import java.util.List;

/**
 * Makes a structure template spawn a prefab at a given location when the structure gets placed.
 */
public class SpawnPrefabsComponent implements Component {

    public List<PrefabToSpawn> prefabsToSpawn;

    @MappedContainer
    public static class PrefabToSpawn {
        public Prefab prefab;
        public Vector3i position;
        public Quat4f rotation = new Quat4f(0,0,0, 0);
    }

}
