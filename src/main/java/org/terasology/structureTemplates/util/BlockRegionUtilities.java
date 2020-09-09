// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.List;

public class BlockRegionUtilities {

    /**
     * @param spawnBlockRegionsComponent
     * @return the bottom center point of a spawnBlockRegion
     */
    public static Vector3i determineBottomCenter(SpawnBlockRegionsComponent spawnBlockRegionsComponent) {
        Vector3f bbCenter = getBoundingBox(spawnBlockRegionsComponent).center();
        Vector3i center = new Vector3i(bbCenter.x, (float) getBoundingBox(spawnBlockRegionsComponent).minY(),
                bbCenter.z);

        return center;
    }

    /**
     * @param spawnBlockRegionsComponent
     * @return the region encompassing the spawnBlockRegion
     */
    public static Region3i getBoundingBox(SpawnBlockRegionsComponent spawnBlockRegionsComponent) {
        List<SpawnBlockRegionsComponent.RegionToFill> regionsToFill = spawnBlockRegionsComponent.regionsToFill;
        if (regionsToFill == null) {
            return null;
        }
        Vector3i max = new Vector3i(regionsToFill.get(0).region.max());
        Vector3i min = new Vector3i(regionsToFill.get(0).region.min());
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
        return Region3i.createFromMinMax(min, max);
    }
}
