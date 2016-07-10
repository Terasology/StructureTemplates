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
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;

/**
 * The entity is a placeholder for a structure.
 *
 * The field {@link #structureTemplateType} specified the type of the structure template that should be placed
 * at the location (See {@link LocationComponent} of this entity.
 *
 * The field {@link #structureTemplateType} references a prefab that gets also referenced by prefabs with a
 * {@link StructureTemplateComponent}. Via this relationship it is thus possible to find possible multiple prefabs
 * that descripbe structures of the wanted type. It will be tried randomly to spawn a structure described by one of
 * thse prefabs till the spawning is successfully.
 *
 * A good {@link StructureTemplateTypeComponent} should always have 1 structure template that has no spawn condition.
 */
public class PendingStructureSpawnComponent implements Component {
    /**
     * Type of the connection point. Two connection poitns can only be connected if they have the same type.
     *
     */
    public Prefab structureTemplateType;
    /**
     * The direction that the front of the placed structure should be facing.
     */
    public Side front;
}
