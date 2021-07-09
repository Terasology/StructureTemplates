// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.gestalt.entitysystem.component.Component;

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
public class PendingStructureSpawnComponent implements Component<PendingStructureSpawnComponent> {
    /**
     * Type of the connection point. Two connection poitns can only be connected if they have the same type.
     *
     */
    public Prefab structureTemplateType;
    /**
     * The direction that the front of the placed structure should be facing.
     */
    public Side front;

    @Override
    public void copy(PendingStructureSpawnComponent other) {
        this.structureTemplateType = other.structureTemplateType;
        this.front = other.front;
    }
}
