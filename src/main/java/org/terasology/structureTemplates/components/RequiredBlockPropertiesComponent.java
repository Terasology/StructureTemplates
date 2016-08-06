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
import org.terasology.structureTemplates.events.GetBlockPredicateEvent;
import org.terasology.world.block.Block;

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
public class RequiredBlockPropertiesComponent implements Component {
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
}
