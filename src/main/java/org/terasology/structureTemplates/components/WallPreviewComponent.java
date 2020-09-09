// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.structureTemplates.internal.components.ReplaceWallItemComponent;

import java.util.List;

/**
 * This component shows a preview of the the wall taht will be placed by items with the {@link
 * ReplaceWallItemComponent}.
 * <p>
 * It is a separte component so that it is possibly to listen for actual modifications of the {@link
 * ReplaceWallItemComponent}.
 */
public class WallPreviewComponent implements Component {
    // TODO it would be saver to let the server calculate the regions
    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public List<Region3i> wallRegions;

}
