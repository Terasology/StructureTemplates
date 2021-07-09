// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Prevents modification of certain absolute regions as long as the entity that owns it exists.
 */
public class ProtectedRegionsComponent implements Component<ProtectedRegionsComponent> {
    @Replicate
    public List<BlockRegion> regions;

    @Override
    public void copy(ProtectedRegionsComponent other) {
        this.regions = other.regions.stream()
                .map(BlockRegion::new)
                .collect(Collectors.toList());
    }
}
