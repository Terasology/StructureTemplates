// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.interfaces;

import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.function.Predicate;

/**
  * Can be obtained via dependency injection ({@link In} annotation). Provides utility functions for checking
  * if an area contains only blocks that matches a certain condition.
  */
public interface BlockRegionChecker {

    /**
     * @return true of the specified condition is true for the specified region afte the transformation got applied.
     */
    boolean allBlocksMatch(BlockRegion untransformedRegion, BlockRegionTransform transform,
                           Predicate<Block> condition);

}
