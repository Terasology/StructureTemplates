// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.reflection.MappedContainer;

import java.util.List;

/**
 * Add this component to a structure template entity in order to have it spawn a structure at the given position of the
 * given structure type. When there are multiple variants of a structure type then one variant that fits at the given
 * location will be picked randomly. A structure will only be tried to spawned once, when it is not possible another
 * strucutre of that type will be tried to spawn.
 * <p>
 * If you want awlays a structure to be spawned for a given structure type, you can add a structure template that has a
 * very low spawn chance (e.g. 1) and that will mainly act as fallback when the other structures are not spawnable.
 * <p>
 * The structure spawning does not happen in the same event processing, but is schedueld to be happen when the system
 * finds time for it. When there are no other outstanding structure spawns this is usually during the next update call
 * of the systems..
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
