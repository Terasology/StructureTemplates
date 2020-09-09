// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.family.AttachedToSurfaceFamily;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.SideDefinedBlockFamily;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.components.BlockRegionTransformComponent;

/**
 * Describes a transformation for a region of blocks like a rotation of 90 degrees or a movement by an offset.
 */
public class BlockRegionTransform {
    private final Vector3i offset;
    /**
     * How often it will be rotated around the y axis by 90 degree. Must be either 0, 1, 2 or 3.
     */
    private int counterClockWiseHorizontal90DegreeRotations = 0;

    private BlockRegionTransform(int counterClockWiseHorizontal90DegreeRotations, Vector3i offset) {
        this.counterClockWiseHorizontal90DegreeRotations = counterClockWiseHorizontal90DegreeRotations;
        this.offset = new Vector3i(offset);
    }

    public static BlockRegionTransform createMovingThenRotating(Vector3i offset, Side startSide, Side targetSide) {
        /*
         * if offset gets added first then it gets transformed by the rotation
         * So to get the same transformation in the form apply rotation first and then
         */
        int rotations = counterClockWiseTurnsFromSideToSide(startSide, targetSide);
        Vector3i transformedOffset = vectorRotatedClockWiseHorizontallyNTimes(offset, rotations);
        return new BlockRegionTransform(rotations, transformedOffset);
    }

    public static BlockRegionTransform createRotationThenMovement(Side startSide, Side targetSide, Vector3i offset) {
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

    private static Vector3i vectorRotatedClockWiseHorizontallyNTimes(Vector3i vectorToTransform, int amount) {
        Vector3i result = new Vector3i(vectorToTransform);
        for (int i = 0; i < amount; i++) {
            int xBackup = result.x();
            int zBackup = result.z();
            result.setX(-zBackup);
            result.setZ(xBackup);
        }
        return result;
    }

    public static BlockRegionTransform createFromComponent(BlockRegionTransformComponent component) {
        return new BlockRegionTransform(component.counterClockWiseHorizontal90DegreeRotations, component.offset);
    }

    /**
     * @return a transformation that does nothing
     */
    public static BlockRegionTransform getTransformationThatDoesNothing() {
        return new BlockRegionTransform(0, new Vector3i(0, 0, 0));
    }

    public Region3i transformRegion(Region3i region) {
        return Region3i.createBounded(transformVector3i(region.min()), transformVector3i(region.max()));
    }

    public Block transformBlock(Block block) {
        BlockFamily blockFamily = block.getBlockFamily();
        if (blockFamily instanceof SideDefinedBlockFamily) {
            SideDefinedBlockFamily sideDefinedBlockFamily = (SideDefinedBlockFamily) blockFamily;
            return sideDefinedBlockFamily.getBlockForSide(transformSide(block.getDirection()));
        } else if (blockFamily instanceof AttachedToSurfaceFamily) {
            // TODO add some proper method to block famility to not have to do this hack
            return blockFamily.getBlockForPlacement(null, transformSide(block.getDirection()), null);
        }
        return block;
    }

    public Side transformSide(Side side) {
        return side.yawClockwise(4 - counterClockWiseHorizontal90DegreeRotations);
    }

    public Vector3i transformVector3i(Vector3i vectorToTransform) {
        Vector3i result = vectorRotatedClockWiseHorizontallyNTimes(vectorToTransform,
                counterClockWiseHorizontal90DegreeRotations);
        result.add(offset);
        return result;
    }

    public Quat4f transformRotation(Quat4f rotation) {
        Side side = transformSide(Side.FRONT);
        Quat4f calculatedRotation = new Quat4f(0, 0, 0, 0);
        switch (side) {
            case FRONT:
                calculatedRotation = new Quat4f(Vector3f.up(), (float) Math.toRadians(0));
                break;
            case RIGHT:
                calculatedRotation = new Quat4f(Vector3f.up(), (float) Math.toRadians(90));
                break;
            case BACK:
                calculatedRotation = new Quat4f(Vector3f.up(), (float) Math.toRadians(180));
                break;
            case LEFT:
                calculatedRotation = new Quat4f(Vector3f.up(), (float) Math.toRadians(270));
                break;
        }
        calculatedRotation.mul(rotation);
        return calculatedRotation;
    }

    public BlockRegionTransformComponent toComponent() {
        BlockRegionTransformComponent component = new BlockRegionTransformComponent();
        component.offset = this.offset;
        component.counterClockWiseHorizontal90DegreeRotations = this.counterClockWiseHorizontal90DegreeRotations;
        return component;
    }
}
