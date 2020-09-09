// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.Replicate;

import java.util.List;

/**
 * Prevents modification of certain absolute regions as long as the entity that owns it exists.
 */
public class ProtectedRegionsComponent implements Component {
    @Replicate
    public List<Region3i> regions;

}
