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

import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.family.AttachedToSurfaceFamily;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.BlockPlacementData;
import org.terasology.engine.world.block.family.SideDefinedBlockFamily;
import org.terasology.structureTemplates.components.BlockRegionTransformComponent;

/**
 * Describes a transformation for a region of blocks like a rotation of 90 degrees or a movement by an offset.
 */
public class BlockRegionTransform {
    /**
     * How often it will be rotated around the y axis by 90 degree. Must be either 0, 1, 2 or 3.
     */
    private int counterClockWiseHorizontal90DegreeRotations = 0;

    private Vector3i offset;

    private BlockRegionTransform(int counterClockWiseHorizontal90DegreeRotations, Vector3ic offset) {
        this.counterClockWiseHorizontal90DegreeRotations = counterClockWiseHorizontal90DegreeRotations;
        this.offset = new Vector3i(offset);
    }

    public static BlockRegionTransform createMovingThenRotating(Vector3ic offset, Side startSide, Side targetSide) {
        /*
         * if offset gets added first then it gets transformed by the rotation
         * So to get the same transformation in the form apply rotation first and then
         */
        int rotations = counterClockWiseTurnsFromSideToSide(startSide, targetSide);
        Vector3i transformedOffset = vectorRotatedClockWiseHorizontallyNTimes(offset, rotations);
        return new BlockRegionTransform(rotations, transformedOffset);
    }

    public static BlockRegionTransform createRotationThenMovement(Side startSide, Side targetSide, Vector3ic offset) {
        return new BlockRegionTransform(counterClockWiseTurnsFromSideToSide(startSide, targetSide), offset);
    }

    private static int sideToCounterClockwiseTurnsFromRight(Side side) {
        switch (side) {
            case RIGHT:
                return 0;
            case BACK:
                return 1;
            case LEFT:
                return 2;
            case FRONT:
                return 3;
            default:
                throw new IllegalArgumentException("not a horizontal side " + side);
        }
    }

    private static int counterClockWiseTurnsFromSideToSide(Side startSide, Side endSide) {
        int turnsFromRightStart = sideToCounterClockwiseTurnsFromRight(startSide);
        int turnsFromRightEnd = sideToCounterClockwiseTurnsFromRight(endSide);
        int turns = turnsFromRightEnd - turnsFromRightStart;
        if (turns < 0) {
            turns += 4;
        }
        return turns;
    }

    public BlockRegion transformRegion(BlockRegion region) {
        return new BlockRegion(transformVector3i(region.getMin(new Vector3i())))
                .union(transformVector3i(region.getMax(new Vector3i())));
    }

    public Block transformBlock(Block block) {
        BlockFamily blockFamily = block.getBlockFamily();
        if (blockFamily instanceof SideDefinedBlockFamily) {
            SideDefinedBlockFamily sideDefinedBlockFamily = (SideDefinedBlockFamily) blockFamily;
            return sideDefinedBlockFamily.getBlockForSide(transformSide(block.getDirection()));
        } else if (blockFamily instanceof AttachedToSurfaceFamily) {
            // TODO add some proper method to block famility to not have to do this hack
            return blockFamily.getBlockForPlacement(new BlockPlacementData(new Vector3i(),
                    transformSide(block.getDirection()), new Vector3f()));
        }
        return block;
    }

    public Side transformSide(Side side) {
        return side.yawClockwise(4 - counterClockWiseHorizontal90DegreeRotations);
    }

    public Vector3i transformVector3i(Vector3ic vectorToTransform) {
        Vector3i result = vectorRotatedClockWiseHorizontallyNTimes(vectorToTransform,
                counterClockWiseHorizontal90DegreeRotations);
        result.add(offset);
        return result;
    }

    private static Vector3i vectorRotatedClockWiseHorizontallyNTimes(Vector3ic vectorToTransform, int amount) {
        Vector3i result = new Vector3i(vectorToTransform);
        for (int i = 0; i < amount; i++) {
            int xBackup = result.x();
            int zBackup = result.z();
            result.x = -zBackup;
            result.z = xBackup;
        }
        return result;
    }

    public Quaternionf transformRotation(Quaternionf rotation) {
        Side side = transformSide(Side.FRONT);
        Quaternionf calculatedRotation = new Quaternionf(0, 0, 0, 0);
        switch (side) {
            case FRONT:
                calculatedRotation = new Quaternionf(new AxisAngle4f(Math.toRadians(0), 0, 1, 0));
                break;
            case RIGHT:
                calculatedRotation = new Quaternionf(new AxisAngle4f(Math.toRadians(90), 0, 1, 0));
                break;
            case BACK:
                calculatedRotation = new Quaternionf(new AxisAngle4f(Math.toRadians(180), 0, 1, 0));
                break;
            case LEFT:
                calculatedRotation = new Quaternionf(new AxisAngle4f(Math.toRadians(270), 0, 1, 0));
                break;
        }
        calculatedRotation.mul(rotation);
        return calculatedRotation;
    }

    public BlockRegionTransformComponent toComponent() {
        BlockRegionTransformComponent component = new BlockRegionTransformComponent();
        component.offset.set(this.offset);
        component.counterClockWiseHorizontal90DegreeRotations = this.counterClockWiseHorizontal90DegreeRotations;
        return component;
    }

    public static BlockRegionTransform createFromComponent(BlockRegionTransformComponent component) {
        return new BlockRegionTransform(component.counterClockWiseHorizontal90DegreeRotations, new Vector3i(component.offset));
    }

    /**
     * @return a transformation that does nothing
     */
    public static BlockRegionTransform getTransformationThatDoesNothing() {
        return new BlockRegionTransform(0, new Vector3i(0, 0, 0));
    }
}
