// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Stores the data to construct a {@link BlockRegionTransform}
 */
public class BlockRegionTransformComponent implements Component {
    public int counterClockWiseHorizontal90DegreeRotations = 0;

    public Vector3i offset;

}
