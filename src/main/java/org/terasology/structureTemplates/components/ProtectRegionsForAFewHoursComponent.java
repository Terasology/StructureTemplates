// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Protects the specified regions of the stucture template against modification by the player for the specified
 * duration.
 *
 */
public class ProtectRegionsForAFewHoursComponent implements Component<ProtectRegionsForAFewHoursComponent> {
    public List<BlockRegion> regions;
    public float hoursToProtect = 2.0f;

    @Override
    public void copy(ProtectRegionsForAFewHoursComponent other) {
        this.regions = other.regions.stream()
                .map(BlockRegion::new)
                .collect(Collectors.toList());
        this.hoursToProtect = other.hoursToProtect;
    }
}
