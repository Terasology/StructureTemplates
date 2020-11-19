/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.structureTemplates.util;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.math.JomlUtil;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.world.block.BlockRegion;

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
        Vector3i center = new Vector3i(new Vector3f(bbCenter.x, (float) getBoundingBox(spawnBlockRegionsComponent).getMinY(),
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
            if (regionToFill.region.getMaxX() > max.x()) {
                max.x = regionToFill.region.getMaxX();
            }
            if (regionToFill.region.getMaxY() > max.y()) {
                max.y = regionToFill.region.getMaxY();
            }
            if (regionToFill.region.getMaxZ() > max.z()) {
                max.z = regionToFill.region.getMaxZ();
            }
            if (regionToFill.region.getMinX() < min.x()) {
                min.x = regionToFill.region.getMinX();
            }
            if (regionToFill.region.getMinY() < min.y()) {
                min.y = regionToFill.region.getMinY();
            }
            if (regionToFill.region.getMinZ() < min.z()) {
                min.z = regionToFill.region.getMinZ();
            }
        }
        return new BlockRegion(min, max);
    }
}
