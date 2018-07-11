/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.components.ProtectedRegionsComponent;

import java.util.Collection;
import java.util.List;

public class ProtectedRegionUtility {

    /**
     * Checks if the specified positions are completely inside the given regionEntity.
     *
     * @param positions
     * @param regionEntity
     * @return
     */
    public static boolean isInProtectedRegion(Collection<Vector3i> positions, EntityRef regionEntity) {
        ProtectedRegionsComponent protectedRegionsComponent = regionEntity.getComponent(ProtectedRegionsComponent.class);
        List<Region3i> protectedRegions = protectedRegionsComponent.regions;
        if (protectedRegions != null) {
            for (Region3i region : protectedRegions) {
                for (Vector3i position : positions) {
                    if (region.encompasses(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
