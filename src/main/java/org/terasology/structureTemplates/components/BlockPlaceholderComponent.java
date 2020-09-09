// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;

/**
 * Block entities that have this component will be copied as the specified blocks when a structure template gets
 * created.
 */
public class BlockPlaceholderComponent implements Component {
    public Block block;
}
