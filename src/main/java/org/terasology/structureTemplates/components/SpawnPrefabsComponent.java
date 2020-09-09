// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
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
        public Quat4f rotation = new Quat4f(0, 0, 0, 0);
    }

}
