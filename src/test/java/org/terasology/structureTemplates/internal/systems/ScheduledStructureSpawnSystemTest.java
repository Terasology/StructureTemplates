// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.junit.Test;
import org.terasology.engine.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import static org.junit.Assert.assertEquals;
import static org.terasology.structureTemplates.internal.systems.ScheduledStructureSpawnSystem.createTransformForIncomingConnectionPoint;

/**
 * Test for {@link ScheduledStructureSpawnSystem}.
 */
public class ScheduledStructureSpawnSystemTest {

    @Test
    public void testCreateTransformForIncomingConnectionPoint() {
        /*-
         * |----------|
         * |          |
         * |i         |
         * |          |
         * |   o      |
         * |----------|
         * i = incoming connection point at (0,0,4) with direction left
         * o = outgoing connection point at (3,0,0) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with front at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |----------|
         * |          |
         * |          |
         * |         o|
         * |    i     |
         * |----------|
         *
         */


        Side direction = Side.FRONT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(0, 0, 4);
        Side incomingConnectionPointDirection = Side.LEFT;

        BlockRegionTransform result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.FRONT, result.transformSide(Side.LEFT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        Vector3i outoingConnectionPointPosition = new Vector3i(3, 0, 0);

        // output point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(104, 200, 303), result.transformVector3i(new Vector3i(3, 0, 0)));

    }


    @Test
    public void testCreateTransformForIncomingConnectionPoint2() {
        /*-
         * |----------|
         * |          |
         * |i         |
         * |          |
         * |   o      |
         * |----------|
         * i = incoming connection point at (0,0,4) with direction left
         * o = outgoing connection point at (3,0,0) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with back at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |----------|
         * |     i    |
         * |o         |
         * |          |
         * |          |
         * |----------|
         *
         */


        Side direction = Side.BACK;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(0, 0, 4);
        Side incomingConnectionPointDirection = Side.LEFT;

        BlockRegionTransform result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.BACK, result.transformSide(Side.LEFT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        Vector3i outoingConnectionPointPosition = new Vector3i(3, 0, 0);

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100 - 4, 200, 300 - 3), result.transformVector3i(new Vector3i(3, 0, 0)));

    }


    @Test
    public void testCreateTransformForIncomingConnectionPoint3() {
        /*-
         * |---------|
         * |    o    |
         * |         |
         * |         |
         * |    i    |
         * |---------|
         * i = incoming connection point at (2,0,0) with direction left
         * o = outgoing connection point at (2,0,5) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with right at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |------------|
         * |            |
         * |o          i|
         * |            |
         * |------------|
         *
         */


        Side direction = Side.RIGHT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(2, 0, 0);
        Side incomingConnectionPointDirection = Side.FRONT;

        BlockRegionTransform result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.BACK));
        assertEquals(new Vector3i(100 - 5, 200, 300), result.transformVector3i(new Vector3i(2, 0, 5)));

    }


    @Test
    public void testCreateTransformForIncomingConnectionPoint4CorrectcoordinateSystem() {
        /*-
         * |------------------------> x-Axis
         * |           Front
         * |
         * |        |---------|
         * |        |    i    |
         * |  Left  |         |   Right
         * |        |         |
         * |        |    o    |
         * |        |---------|
         * |
         * |            Back
         * |
         * v z-Axis,
         *
         * i = incoming connection point at (2,0,0) with direction left
         * o = outgoing connection point at (2,0,5) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with right at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         * |------------------------> x-Axis
         * |           Front
         * |
         * |        |------------|
         * |        |            |
         * |  Left  |o          i|  Right
         * |        |            |
         * |        |------------|
         * |
         * |            Back
         * |
         * v z-Axis,
         *
         *
         */


        Side direction = Side.RIGHT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(2, 0, 0);
        Side incomingConnectionPointDirection = Side.FRONT;

        BlockRegionTransform result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.BACK));
        assertEquals(new Vector3i(100 - 5, 200, 300), result.transformVector3i(new Vector3i(2, 0, 5)));

    }
}
