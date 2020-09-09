// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.interfaces;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;

import java.util.function.Predicate;

/**
 * Can be obtained via dependency injection ({@link In} annotation). Provides utility functions for checking if an area
 * contains only blocks that matches a certain condition.
 */
public interface BlockPredicateProvider {

    Predicate<Block> getBlockPredicate(Prefab condition) throws IllegalArgumentException;

    Predicate<Block> getBlockPredicate(String prefabUrn) throws IllegalArgumentException;
}
