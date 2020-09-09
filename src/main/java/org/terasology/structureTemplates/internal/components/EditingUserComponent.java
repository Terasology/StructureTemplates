// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

/**
 * This component allows you to desribe that a user is currently editing something.
 * <p>
 * How this editing looks like is not specified. See the field {@link #editProcessEntity} for more details.
 */
public class EditingUserComponent implements Component {
    /**
     * A entity that must be destroyed to end the current editing of the user.
     * <p>
     * It should be a non persistent entity that has this entity as owner.
     */
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef editProcessEntity = EntityRef.NULL;


}
