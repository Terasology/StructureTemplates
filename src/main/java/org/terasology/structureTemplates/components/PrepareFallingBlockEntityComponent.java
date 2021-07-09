// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.joml.Vector3i;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.component.Component;

public class PrepareFallingBlockEntityComponent implements Component<PrepareFallingBlockEntityComponent> {
    public Block block;
    public Vector3i targetPosition;
    public long fallDurationInMs;

    @Override
    public void copy(PrepareFallingBlockEntityComponent other) {
        this.block = other.block;
        this.targetPosition = new Vector3i(other.targetPosition);
        this.fallDurationInMs = other.fallDurationInMs;
    }
}
