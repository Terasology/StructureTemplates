// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;


import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.structureTemplates.util.BlockRegionUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class BlockRegionBoundingBoxTest {
    private Block blockA;
    private Block blockB;

    @BeforeEach
    public void prepare() {
        blockA = new Block();
        blockA.setUri(new BlockUri("a:a"));
        blockB = new Block();
        blockB.setUri(new BlockUri("a:b"));
    }

    @Test
    public void testBoundingBox() {
        SpawnBlockRegionsComponent blockRegionA = new SpawnBlockRegionsComponent();
        SpawnBlockRegionsComponent.RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        SpawnBlockRegionsComponent.RegionToFill regionB = createRegion(blockA, 3, 0, -1, 3, 1, 5);
        SpawnBlockRegionsComponent.RegionToFill regionC = createRegion(blockA, 4, 0, -1, 6, 1, 5);
        blockRegionA.regionsToFill.add(regionA);
        blockRegionA.regionsToFill.add(regionB);
        blockRegionA.regionsToFill.add(regionC);
        BlockRegion bb = BlockRegionUtilities.getBoundingBox(blockRegionA);
        assertEquals(bb.getMax(new Vector3i()), new Vector3i(6, 1, 5));
        assertEquals(bb.getMin(new Vector3i()), new Vector3i(2, 0, -1));


    }
    private SpawnBlockRegionsComponent.RegionToFill createRegion(Block blockType, int minX, int minY, int minZ,
                                                                 int maxX, int maxY, int maxZ) {
        SpawnBlockRegionsComponent.RegionToFill r = new SpawnBlockRegionsComponent.RegionToFill();
        r.region = new BlockRegion(new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));
        r.blockType = blockType;
        return r;
    }
}
