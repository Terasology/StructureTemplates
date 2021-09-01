// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.List;

public final class BlockRegionUtilities {

    private BlockRegionUtilities() {
    }

    /**
     * @param spawnBlockRegionsComponent
     * @return the bottom center point of a spawnBlockRegion
     */
    public static Vector3i determineBottomCenter(SpawnBlockRegionsComponent spawnBlockRegionsComponent) {
        Vector3f bbCenter = getBoundingBox(spawnBlockRegionsComponent).center(new Vector3f());
        Vector3i center = new Vector3i(new Vector3f(bbCenter.x, (float) getBoundingBox(spawnBlockRegionsComponent).minY(),
            bbCenter.z), RoundingMode.FLOOR);
        return center;
    }

    /**
     * @param spawnBlockRegionsComponent
     * @return the region encompassing the spawnBlockRegion
     */
    public static BlockRegion getBoundingBox(SpawnBlockRegionsComponent spawnBlockRegionsComponent) {
        List<SpawnBlockRegionsComponent.RegionToFill> regionsToFill = spawnBlockRegionsComponent.regionsToFill;
        if (regionsToFill == null) {
            return null;
        }
        Vector3i max = new Vector3i(regionsToFill.get(0).region.getMax(new Vector3i()));
        Vector3i min = new Vector3i(regionsToFill.get(0).region.getMin(new Vector3i()));
        for (SpawnBlockRegionsComponent.RegionToFill regionToFill : regionsToFill) {
            if (regionToFill.region.maxX() > max.x()) {
                max.x = regionToFill.region.maxX();
            }
            if (regionToFill.region.maxY() > max.y()) {
                max.y = regionToFill.region.maxY();
            }
            if (regionToFill.region.maxZ() > max.z()) {
                max.z = regionToFill.region.maxZ();
            }
            if (regionToFill.region.minX() < min.x()) {
                min.x = regionToFill.region.minX();
            }
            if (regionToFill.region.minY() < min.y()) {
                min.y = regionToFill.region.minY();
            }
            if (regionToFill.region.minZ() < min.z()) {
                min.z = regionToFill.region.minZ();
            }
        }
        return new BlockRegion(min, max);
    }
}
