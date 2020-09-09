// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;

import java.util.function.Predicate;

/**
 * Can be added to entities with the {@link BlockPredicateComponent} to define a condition that requires certain block
 * properties to have certain values.
 * <p>
 * Send a {@link GetBlockPredicateEvent} to a entity with this component to get a {@link Block} accepting {@link
 * Predicate}.
 * <p>
 * Fields of this component that are null will be ignored.
 */
public class RequiredBlockPropertiesComponent implements Component {
    /**
     * If not null, the {@link Block}s in the specified required are required to have {@link Block#penetrable} set to
     * the specified value.
     */
    public Boolean penetrable = null;

    /**
     * If not null, the {@link Block}s in the specified required are required to have {@link Block#liquid} set to the
     * specified value.
     */
    public Boolean liquid = null;


    public Boolean loaded = null;
}
