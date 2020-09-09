// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
