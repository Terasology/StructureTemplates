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
package org.terasology.structureTemplates.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;
import org.terasology.world.block.BlockRegion;

import java.util.List;
import java.util.function.Predicate;

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
        public BlockRegion region;
    }
}
