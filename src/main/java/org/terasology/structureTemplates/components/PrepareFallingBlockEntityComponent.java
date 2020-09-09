// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

public class PrepareFallingBlockEntityComponent implements Component {
    public Block block;
    public Vector3i targetPosition;
    public long fallDurationInMs;
}
