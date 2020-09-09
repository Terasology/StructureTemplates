// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Region3i;
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
        ProtectedRegionsComponent protectedRegionsComponent =
                regionEntity.getComponent(ProtectedRegionsComponent.class);
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
