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
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.world.block.Block;
import org.terasology.structureTemplates.components.BlockPredicateComponent;

import java.util.function.Predicate;

/**
 * Can be sent to entities with the {@link BlockPredicateComponent} to get a {@link Predicate} for {@link Block}s.
 *
 * The event is consumable so that systems hae the oppertunity to prevent lower priority systems from running.
 */
public class GetBlockPredicateEvent extends AbstractConsumableEvent {
    /**
     * Gets modified by event handlers. Returns true by default
     */
    public Predicate<Block> predicate = block -> true;
}
