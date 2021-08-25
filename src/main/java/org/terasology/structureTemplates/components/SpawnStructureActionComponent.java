// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Add this component to an item to make it spawn a structure on activation.
 */
public class SpawnStructureActionComponent implements Component<SpawnStructureActionComponent> {

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public BlockRegion unconfirmSpawnErrorRegion;

    @Override
    public void copyFrom(SpawnStructureActionComponent other) {
        this.unconfirmSpawnErrorRegion = new BlockRegion(other.unconfirmSpawnErrorRegion);
    }
}
