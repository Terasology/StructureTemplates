// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

/**
 * Gets added to a client entity when that client decides to edit the copy region of a structure template..
 */
public class EditTemplateRegionProcessComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef structureTemplateEditor = EntityRef.NULL;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public boolean recordBlockAddition;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public boolean recordBlockRemoval;
}
