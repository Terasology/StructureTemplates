// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fills regions with blocks when a entity with component receives a {@link SpawnStructureEvent}.
 */
public class SpawnBlockRegionsComponent implements Component<SpawnBlockRegionsComponent> {
    public List<RegionToFill> regionsToFill = new ArrayList<>();

    @Override
    public void copyFrom(SpawnBlockRegionsComponent other) {
        this.regionsToFill = other.regionsToFill.stream()
                .map(RegionToFill::copy)
                .collect(Collectors.toList());
    }

    @MappedContainer
    public static class RegionToFill {
        public BlockRegion region;
        public Block blockType;

        RegionToFill copy() {
            RegionToFill newRegion = new RegionToFill();
            newRegion.region = new BlockRegion(this.region);
            newRegion.blockType = blockType;
            return newRegion;
        }
    }
}
