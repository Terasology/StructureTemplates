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
package org.terasology.structureTemplates.components;


import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import static org.junit.Assert.assertEquals;

public class BlockRegionBoundingBoxTest {
    private Block blockA;
    private Block blockB;

    @Before
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
        Region3i bb = blockRegionA.getBoundingBox();
        assertEquals(bb.max(), new Vector3i(6, 1, 5));
        assertEquals(bb.min(), new Vector3i(2, 0, -1));


    }
    private SpawnBlockRegionsComponent.RegionToFill createRegion(Block blockType, int minX, int minY, int minZ,
                                                                 int maxX, int maxY, int maxZ) {
        SpawnBlockRegionsComponent.RegionToFill r = new SpawnBlockRegionsComponent.RegionToFill();
        r.region = Region3i.createBounded(new Vector3i(minX, minY, minZ), new Vector3i(maxX, maxY, maxZ));
        r.blockType = blockType;
        return r;
    }
}
