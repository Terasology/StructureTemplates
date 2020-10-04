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
        Vector3i max = new Vector3i(JomlUtil.from(regionsToFill.get(0).region.max()));
        Vector3i min = new Vector3i(JomlUtil.from(regionsToFill.get(0).region.min()));
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
