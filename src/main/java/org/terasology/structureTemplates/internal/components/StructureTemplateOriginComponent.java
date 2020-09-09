// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;

import java.util.List;

/**
 * Used to describe an block region location
 */
@ForceBlockActive
public class StructureTemplateOriginComponent implements Component {


    /**
     * Edited regions in absolute coordinates
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public List<Region3i> absoluteTemplateRegions = Lists.newArrayList();

}
