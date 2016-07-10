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
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;

import java.util.List;

/**
 * Add this component to a structure template entity in order to have it spawn a structure at the given position
 * of the given structure type. When there are multiple variants of a structure type then one variant that
 * fits at the given location will be picked randomly.
 *
 * The structure spawning does not happen in the same event processing, but is schedueld
 * to be happen when the system finds time for it. When there are no other outstanding structure spawns this is
 * usually during the next update call of the systems..
 */
public class ScheduleStructurePlacementComponent implements Component {
    public List<PlacementToSchedule> placementsToSchedule;

    @MappedContainer
    public static class PlacementToSchedule {
        /**
         * Prefab with the component {@link StructureTemplateTypeComponent}
         */
        public Prefab structureTemplateType;

        /**
         * The location where the structure will be spawned in the coordinate system of the template with this
         * component.
         */
        public Vector3i position;


        /**
         * The direction in which the front of the spawned structure should point to.
         */
        public Side front;
    }
}
