// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

/**
 * Add this component to an item to make it spawn a structure on activation.
 */
public class SpawnStructureActionComponent implements Component {

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public Region3i unconfirmSpawnErrorRegion;

}
