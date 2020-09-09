// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;

/**
 * Makes a structure placholder block a structure placeholder block.
 */
@ForceBlockActive // Force block active so that entity remains during interaction
public class StructurePlaceholderComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public Prefab selectedPrefab;
}
