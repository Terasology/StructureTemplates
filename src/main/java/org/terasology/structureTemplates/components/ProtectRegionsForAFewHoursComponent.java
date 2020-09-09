// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;

import java.util.List;

/**
 * Protects the specified regions of the stucture template against modification by the player for the specified
 * duration.
 */
public class ProtectRegionsForAFewHoursComponent implements Component {
    public List<Region3i> regions;
    public float hoursToProtect = 2.0f;
}
