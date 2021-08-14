// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.joml.Quaternionf;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Makes a structure template spawn a prefab at a given location when the structure gets placed.
 */
public class SpawnPrefabsComponent implements Component<SpawnPrefabsComponent> {

    public List<PrefabToSpawn> prefabsToSpawn;

    @Override
    public void copyFrom(SpawnPrefabsComponent other) {
        this.prefabsToSpawn = other.prefabsToSpawn.stream()
                .map(PrefabToSpawn::copy)
                .collect(Collectors.toList());
    }

    @MappedContainer
    public static class PrefabToSpawn {
        public Prefab prefab;
        public Vector3i position;
        public Quaternionf rotation = new Quaternionf();

        PrefabToSpawn copy(){
            PrefabToSpawn newSpawn = new PrefabToSpawn();
            newSpawn.prefab = this.prefab;
            newSpawn.position = new Vector3i(this.position);
            newSpawn.rotation = new Quaternionf(this.rotation);
            return newSpawn;
        }
    }
}
