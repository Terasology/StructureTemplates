// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Block entities that have this component will be copied as the specified blocks when a structure template gets
 * created.
 *
 */
public class BlockPlaceholderComponent implements Component<BlockPlaceholderComponent> {
    public Block block;

    @Override
    public void copyFrom(BlockPlaceholderComponent other) {
        this.block = other.block;
    }
}
