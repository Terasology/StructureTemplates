// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Stores the data to construct a {@link BlockRegionTransform}
 */
public class BlockRegionTransformComponent implements Component<BlockRegionTransformComponent> {
    public int counterClockWiseHorizontal90DegreeRotations = 0;

    public Vector3i offset;

    @Override
    public void copy(BlockRegionTransformComponent other) {
        this.counterClockWiseHorizontal90DegreeRotations = other.counterClockWiseHorizontal90DegreeRotations;
        this.offset = other.offset;
    }
}
