// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.gestalt.entitysystem.component.Component;

public class CompletionTimeComponent implements Component<CompletionTimeComponent> {
    public long completionDelay;

    @Override
    public void copyFrom(CompletionTimeComponent other) {
        this.completionDelay = other.completionDelay;
    }
}
