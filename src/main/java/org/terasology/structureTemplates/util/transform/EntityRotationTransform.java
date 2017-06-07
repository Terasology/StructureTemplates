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
package org.terasology.structureTemplates.util.transform;

import org.terasology.math.Side;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Describes the rotation for spawnPrefabs to maintain orientation along with the structure.
 */
public class EntityRotationTransform {

    public static Quat4f calculateRotation(Side side, Quat4f rotation) {
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
}
