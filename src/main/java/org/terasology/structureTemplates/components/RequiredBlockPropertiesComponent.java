// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;

import java.util.function.Predicate;

/**
 * Can be added to entities with the {@link BlockPredicateComponent} to define a condition that requires
 * certain block properties to have certain values.
 *
 * Send a {@link GetBlockPredicateEvent} to a entity with this component to get a {@link Block} accepting
 * {@link Predicate}.
 *
 * Fields of this component that are null will be ignored.
 */
public class RequiredBlockPropertiesComponent implements Component<RequiredBlockPropertiesComponent> {
    /**
     * If not null, the {@link Block}s in the specified required are required to have {@link Block#penetrable} set to
     * the specified value.
     */
    public Boolean penetrable = null;

    /**
     * If not null, the {@link Block}s in the specified required are required to have {@link Block#liquid} set to
     * the specified value.
     */
    public Boolean liquid = null;


    public Boolean loaded = null;

    @Override
    public void copy(RequiredBlockPropertiesComponent other) {
        this.penetrable = other.penetrable;
        this.liquid = other.liquid;
        this.loaded = other.loaded;
    }
}
