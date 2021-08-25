// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * TODO: add javadoc
 */
public class BuildStructureCounterComponent implements Component<BuildStructureCounterComponent> {
    public int iter;

    @Override
    public void copyFrom(BuildStructureCounterComponent other) {
        this.iter = other.iter;
    }
}
