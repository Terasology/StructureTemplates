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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for merging touching {@link BlockRegion} and {@link SpawnBlockRegionsComponent.RegionToFill} together.
 *
 * Typically you create first a list of region 3i that contain each a single block position. Then you use the
 * methods of this class to merge them together to fewer larget regions that cover the same blocks.
 */
public final class RegionMergeUtil {
    private RegionMergeUtil() {

    }

    public static void mergeRegionsToFill(List<SpawnBlockRegionsComponent.RegionToFill> regionsToFill) {
        mergeRegionsToFillByX(regionsToFill);
        mergeRegionsToFillByY(regionsToFill);
        mergeRegionsToFillByZ(regionsToFill);
    }


    public static List<BlockRegion> mergePositionsIntoRegions(Set<Vector3i> positionsInTemplate) {
        List<BlockRegion> newTemplateRegions = new ArrayList<>();
        for (Vector3i position : positionsInTemplate) {
            newTemplateRegions.add(new BlockRegion(position, position));
        }
        RegionMergeUtil.mergeSingleBlockRegions(newTemplateRegions);
        return newTemplateRegions;
    }

    private static void mergeSingleBlockRegions(List<BlockRegion> regions) {
        mergeRegionsByX(regions);
        mergeRegionsByY(regions);
        mergeRegionsByZ(regions);
    }


    public static Set<Vector3i> positionsOfRegions(List<BlockRegion> originalRegions) {
        Set<Vector3i> positionsInTemplate = new HashSet<>();
        for (BlockRegion region : originalRegions) {
            for (Vector3ic position : BlockRegionIterable.region(region).build()) {
                positionsInTemplate.add(new Vector3i(position));
            }
        }
        return positionsInTemplate;
    }


    static void mergeRegionsToFillByDimension(List<SpawnBlockRegionsComponent.RegionToFill> regions, RegionDimension dimensionToMerge,
                                              RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionToFillComparator().thenComparing(thirdDimension.regionToFillComparator()).
            thenComparing(dimensionToMerge.regionToFillComparator()));
        List<SpawnBlockRegionsComponent.RegionToFill> newList = new ArrayList<>();
        SpawnBlockRegionsComponent.RegionToFill previous = null;
        for (SpawnBlockRegionsComponent.RegionToFill r : regions) {
            boolean canMerge = previous != null && dimensionToMerge.getMax(previous.region) == dimensionToMerge.getMin(r.region) - 1
                && secondaryDimension.getMin(r.region) == secondaryDimension.getMin(previous.region)
                && secondaryDimension.getMax(r.region) == secondaryDimension.getMax(previous.region)
                && thirdDimension.getMin(r.region) == thirdDimension.getMin(previous.region)
                && thirdDimension.getMax(r.region) == thirdDimension.getMax(previous.region)
                && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = dimensionToMerge.regionCopyWithMaxSetTo(previous.region, dimensionToMerge.getMax(r.region));
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsByDimension(List<BlockRegion> regions, RegionDimension dimensionToMerge,
                                        RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionComparator().thenComparing(thirdDimension.regionComparator()).
            thenComparing(dimensionToMerge.regionComparator()));
        List<BlockRegion> newList = new ArrayList<>();
        BlockRegion previous = null;
        for (BlockRegion r : regions) {
            boolean canMerge = previous != null && dimensionToMerge.getMax(previous) == dimensionToMerge.getMin(r) - 1
                && secondaryDimension.getMin(r) == secondaryDimension.getMin(previous)
                && secondaryDimension.getMax(r) == secondaryDimension.getMax(previous)
                && thirdDimension.getMin(r) == thirdDimension.getMin(previous)
                && thirdDimension.getMax(r) == thirdDimension.getMax(previous);
            if (canMerge) {
                // Remove previous:
                newList.remove(newList.size() - 1);
                previous = dimensionToMerge.regionCopyWithMaxOfSecond(previous, r);
                newList.add(previous);
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsToFillByX(List<SpawnBlockRegionsComponent.RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.X, RegionDimension.Y, RegionDimension.Z);
    }

    static void mergeRegionsToFillByY(List<SpawnBlockRegionsComponent.RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.Y, RegionDimension.Z, RegionDimension.X);
    }

    static void mergeRegionsToFillByZ(List<SpawnBlockRegionsComponent.RegionToFill> regions) {
        mergeRegionsToFillByDimension(regions, RegionDimension.Z, RegionDimension.X, RegionDimension.Y);
    }


    static void mergeRegionsByX(List<BlockRegion> regions) {
        mergeRegionsByDimension(regions, RegionDimension.X, RegionDimension.Y, RegionDimension.Z);
    }

    static void mergeRegionsByY(List<BlockRegion> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Y, RegionDimension.Z, RegionDimension.X);
    }

    static void mergeRegionsByZ(List<BlockRegion> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Z, RegionDimension.X, RegionDimension.Y);
    }


    private enum RegionDimension {
        X {
            public int getMin(BlockRegion r) {
                return r.getMinX();
            }

            public int getMax(BlockRegion r) {
                return r.getMaxX();
            }

            public BlockRegion regionCopyWithMaxSetTo(BlockRegion r, int newMax) {
                Vector3i max = new Vector3i(newMax, r.getMaxY(), r.getMaxZ());
                return new BlockRegion().union(r.getMin(new Vector3i())).union(max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.getMinX());
            }

            public Comparator<BlockRegion> regionComparator() {
                return Comparator.comparing(r -> r.getMinX());
            }
        },
        Y {
            public int getMin(BlockRegion r) {
                return r.getMinY();
            }

            public int getMax(BlockRegion r) {
                return r.getMaxY();
            }

            public BlockRegion regionCopyWithMaxSetTo(BlockRegion r, int newMax) {
                Vector3i max = new Vector3i(r.getMaxX(), newMax, r.getMaxZ());
                return new BlockRegion().union(r.getMin(new Vector3i())).union(max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.getMinY());
            }

            public Comparator<BlockRegion> regionComparator() {
                return Comparator.comparing(r -> r.getMinY());
            }
        },
        Z {
            public int getMin(BlockRegion r) {
                return r.getMinZ();
            }

            public int getMax(BlockRegion r) {
                return r.getMaxZ();
            }

            public BlockRegion regionCopyWithMaxSetTo(BlockRegion r, int newMax) {
                Vector3i max = new Vector3i(r.getMaxX(), r.getMaxY(), newMax);
                return new BlockRegion().union(r.getMin(new Vector3i())).union(max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.getMinZ());
            }

            public Comparator<BlockRegion> regionComparator() {
                return Comparator.comparing(r -> r.getMinZ());
            }
        };

        public abstract int getMin(BlockRegion r);

        public abstract int getMax(BlockRegion r);

        public abstract BlockRegion regionCopyWithMaxSetTo(BlockRegion r, int newMax);

        public BlockRegion regionCopyWithMaxOfSecond(BlockRegion regionToCopy, BlockRegion regionWithMax) {
            int newMax = getMax(regionWithMax);
            return regionCopyWithMaxSetTo(regionToCopy, newMax);
        }

        public abstract Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator();

        public abstract Comparator<BlockRegion> regionComparator();
    }

}
