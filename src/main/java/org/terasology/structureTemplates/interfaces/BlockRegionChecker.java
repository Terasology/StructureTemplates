/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.structureTemplates.interfaces;

import org.terasology.math.Region3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.block.Block;

import java.util.function.Predicate;

/**
  * Can be obtained via dependency injection ({@link In} annotation). Provides utility functions for checking
  * if an area contains only blocks that matches a certain condition.
  */
public interface BlockRegionChecker {

    /**
     * @return true of the specified condition is true for the specified region afte the transformation got applied.
     */
    boolean allBlocksMatch(Region3i untransformedRegion, BlockRegionTransform transform,
                                  Predicate<Block> condition);

}
