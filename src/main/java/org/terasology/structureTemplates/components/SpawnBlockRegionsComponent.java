// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.block.Block;
import org.terasology.nui.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills regions with blocks when a entity with component receives a {@link SpawnStructureEvent}.
 */
public class SpawnBlockRegionsComponent implements Component {
    public List<RegionToFill> regionsToFill = new ArrayList<>();

    @MappedContainer
    public static class RegionToFill {
        public Region3i region;
        public Block blockType;
    }
}
