// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import com.google.common.collect.Lists;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to describe an block region location
 */
@ForceBlockActive
public class StructureTemplateOriginComponent implements Component<StructureTemplateOriginComponent> {

    /**
     * Edited regions in absolute coordinates
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public List<BlockRegion> absoluteTemplateRegions = Lists.newArrayList();

    @Override
    public void copy(StructureTemplateOriginComponent other) {
        this.absoluteTemplateRegions = other.absoluteTemplateRegions.stream()
                .map(BlockRegion::new)
                .collect(Collectors.toList());
    }
}
