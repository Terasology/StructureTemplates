// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * When a entity with this component receives a {@link CheckSpawnConditionEvent} then the specified checks will be
 * performed. The result of the check will be stored in the {@link CheckSpawnConditionEvent} event.
 *
 * To perform the check, a {@link GetBlockPredicateEvent} event will be sent to an entity of the
 * specified condition prefab. The returned {@link Predicate} checked for all block positions in the transformed region.
 *
 * The region will be transformed by what got specified in the {@link CheckSpawnConditionEvent}.
 *
 * The condition prefab must contain the component {@link BlockPredicateComponent}.
 */
public class CheckBlockRegionConditionComponent implements Component<CheckBlockRegionConditionComponent> {
    public List<BlockRegionConditionCheck> checksToPerform;

    @Override
    public void copy(CheckBlockRegionConditionComponent other) {
        this.checksToPerform = other.checksToPerform.stream()
                .map(BlockRegionConditionCheck::copy)
                .collect(Collectors.toList());
    }

    @MappedContainer
    public static class BlockRegionConditionCheck {
        /**
         * Prefab with {@link BlockPredicateComponent} and some other components that describe the condition.
         */
        public Prefab condition;
        /**
         * Region which should be checked against the condition.
         */
        public BlockRegion region;

        BlockRegionConditionCheck copy() {
            BlockRegionConditionCheck newObj = new BlockRegionConditionCheck();
            newObj.condition = this.condition;
            newObj.region = new BlockRegion(this.region);
            return newObj;
        }
    }
}
