// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for merging touching {@link Region3i} and {@link SpawnBlockRegionsComponent.RegionToFill} together.
 * <p>
 * Typically you create first a list of region 3i that contain each a single block position. Then you use the methods of
 * this class to merge them together to fewer larget regions that cover the same blocks.
 */
public class RegionMergeUtil {

    public static void mergeRegionsToFill(List<SpawnBlockRegionsComponent.RegionToFill> regionsToFill) {
        mergeRegionsToFillByX(regionsToFill);
        mergeRegionsToFillByY(regionsToFill);
        mergeRegionsToFillByZ(regionsToFill);
    }


    public static List<Region3i> mergePositionsIntoRegions(Set<Vector3i> positionsInTemplate) {
        List<Region3i> newTemplateRegions = new ArrayList<>();
        for (Vector3i position : positionsInTemplate) {
            newTemplateRegions.add(Region3i.createFromMinMax(position, position));
        }
        RegionMergeUtil.mergeSingleBlockRegions(newTemplateRegions);
        return newTemplateRegions;
    }

    private static void mergeSingleBlockRegions(List<Region3i> regions) {
        mergeRegionsByX(regions);
        mergeRegionsByY(regions);
        mergeRegionsByZ(regions);
    }


    public static Set<Vector3i> positionsOfRegions(List<Region3i> originalRegions) {
        Set<Vector3i> positionsInTemplate = new HashSet<>();
        for (Region3i region : originalRegions) {
            for (Vector3i position : region) {
                positionsInTemplate.add(position);
            }
        }
        return positionsInTemplate;
    }


    static void mergeRegionsToFillByDimension(List<SpawnBlockRegionsComponent.RegionToFill> regions,
                                              RegionDimension dimensionToMerge,
                                              RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionToFillComparator().thenComparing(thirdDimension.regionToFillComparator()).
                thenComparing(dimensionToMerge.regionToFillComparator()));
        List<SpawnBlockRegionsComponent.RegionToFill> newList = new ArrayList<>();
        SpawnBlockRegionsComponent.RegionToFill previous = null;
        for (SpawnBlockRegionsComponent.RegionToFill r : regions) {
            boolean canMerge =
                    previous != null && dimensionToMerge.getMax(previous.region) == dimensionToMerge.getMin(r.region) - 1
                    && secondaryDimension.getMin(r.region) == secondaryDimension.getMin(previous.region)
                    && secondaryDimension.getMax(r.region) == secondaryDimension.getMax(previous.region)
                    && thirdDimension.getMin(r.region) == thirdDimension.getMin(previous.region)
                    && thirdDimension.getMax(r.region) == thirdDimension.getMax(previous.region)
                    && r.blockType.equals(previous.blockType);
            if (canMerge) {
                previous.region = dimensionToMerge.regionCopyWithMaxSetTo(previous.region,
                        dimensionToMerge.getMax(r.region));
            } else {
                newList.add(r);
                previous = r;
            }
        }
        regions.clear();
        regions.addAll(newList);
    }

    static void mergeRegionsByDimension(List<Region3i> regions, RegionDimension dimensionToMerge,
                                        RegionDimension secondaryDimension, RegionDimension thirdDimension) {
        regions.sort(secondaryDimension.regionComparator().thenComparing(thirdDimension.regionComparator()).
                thenComparing(dimensionToMerge.regionComparator()));
        List<Region3i> newList = new ArrayList<>();
        Region3i previous = null;
        for (Region3i r : regions) {
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


    static void mergeRegionsByX(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.X, RegionDimension.Y, RegionDimension.Z);
    }

    static void mergeRegionsByY(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Y, RegionDimension.Z, RegionDimension.X);
    }

    static void mergeRegionsByZ(List<Region3i> regions) {
        mergeRegionsByDimension(regions, RegionDimension.Z, RegionDimension.X, RegionDimension.Y);
    }


    private enum RegionDimension {
        X {
            public int getMin(Region3i r) {
                return r.minX();
            }

            public int getMax(Region3i r) {
                return r.maxX();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(newMax, r.maxY(), r.maxZ());
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minX());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minX());
            }
        },
        Y {
            public int getMin(Region3i r) {
                return r.minY();
            }

            public int getMax(Region3i r) {
                return r.maxY();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(r.maxX(), newMax, r.maxZ());
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minY());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minY());
            }
        },
        Z {
            public int getMin(Region3i r) {
                return r.minZ();
            }

            public int getMax(Region3i r) {
                return r.maxZ();
            }

            public Region3i regionCopyWithMaxSetTo(Region3i r, int newMax) {
                Vector3i max = new Vector3i(r.maxX(), r.maxY(), newMax);
                return Region3i.createBounded(r.min(), max);
            }

            public Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator() {
                return Comparator.comparing(r -> r.region.minZ());
            }

            public Comparator<Region3i> regionComparator() {
                return Comparator.comparing(r -> r.minZ());
            }
        };

        public abstract int getMin(Region3i r);

        public abstract int getMax(Region3i r);

        public abstract Region3i regionCopyWithMaxSetTo(Region3i r, int newMax);

        public Region3i regionCopyWithMaxOfSecond(Region3i regionToCopy, Region3i regionWithMax) {
            int newMax = getMax(regionWithMax);
            return regionCopyWithMaxSetTo(regionToCopy, newMax);
        }

        public abstract Comparator<SpawnBlockRegionsComponent.RegionToFill> regionToFillComparator();

        public abstract Comparator<Region3i> regionComparator();
    }

}
