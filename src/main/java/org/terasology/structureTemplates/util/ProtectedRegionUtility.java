// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.structureTemplates.components.ProtectedRegionsComponent;

import java.util.Collection;
import java.util.List;

public final class ProtectedRegionUtility {

    private ProtectedRegionUtility() { }

    /**
     * Checks if the specified positions are completely inside the given regionEntity.
     *
     * @param positions
     * @param regionEntity
     * @return
     */
    public static boolean isInProtectedRegion(Collection<? extends Vector3ic> positions, EntityRef regionEntity) {
        ProtectedRegionsComponent protectedRegionsComponent =
                regionEntity.getComponent(ProtectedRegionsComponent.class);
        List<BlockRegion> protectedRegions = protectedRegionsComponent.regions;
        if (protectedRegions != null) {
            for (BlockRegion region : protectedRegions) {
                for (Vector3ic position : positions) {
                    if (region.contains(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
