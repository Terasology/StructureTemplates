// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Region3i;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;

import java.util.List;
import java.util.function.Predicate;

/**
 * When a entity with this component receives a {@link CheckSpawnConditionEvent} then the specified checks will be
 * performed. The result of the check will be stored in the {@link CheckSpawnConditionEvent} event.
 * <p>
 * To perform the check, a {@link GetBlockPredicateEvent} event will be sent to an entity of the specified condition
 * prefab. The returned {@link Predicate} checked for all block positions in the transformed region.
 * <p>
 * The region will be transformed by what got specified in the {@link CheckSpawnConditionEvent}.
 * <p>
 * The condition prefab must contain the component {@link BlockPredicateComponent}.
 */
public class CheckBlockRegionConditionComponent implements Component {
    public List<BlockRegionConditionCheck> checksToPerform;

    @MappedContainer
    public static class BlockRegionConditionCheck {
        /**
         * Prefab with {@link BlockPredicateComponent} and some other components that describe the condition.
         */
        public Prefab condition;
        /**
         * Region which should be checked against the condition.
         */
        public Region3i region;
    }
}
