// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;

import java.util.function.Predicate;

/**
 * Marks a prefab as describing a condition for blocks. Send a {@link GetBlockPredicateEvent} to an entity with this
 * component to get a  {@link Predicate} that takes a {@link Block}.
 */
public class BlockPredicateComponent implements Component {
}
