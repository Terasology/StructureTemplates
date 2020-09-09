// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.world.block.Block;
import org.terasology.structureTemplates.components.BlockPredicateComponent;

import java.util.function.Predicate;

/**
 * Can be sent to entities with the {@link BlockPredicateComponent} to get a {@link Predicate} for {@link Block}s.
 * <p>
 * The event is consumable so that systems hae the oppertunity to prevent lower priority systems from running.
 */
public class GetBlockPredicateEvent extends AbstractConsumableEvent {
    /**
     * Gets modified by event handlers. Returns true by default
     */
    public Predicate<Block> predicate = block -> true;
}
