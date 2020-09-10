// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.reflection.MappedContainer;

import java.util.Collections;
import java.util.List;

/**
 * TODO: add javadoc
 */
public class BuildStepwiseStructureComponent implements Component {
    private final List<BuildStep> buildSteps;

    public BuildStepwiseStructureComponent(List<BuildStep> buildSteps) {
        this.buildSteps = buildSteps;
    }

    public BuildStepwiseStructureComponent() {
        this.buildSteps = Collections.emptyList();
    }

    public List<BuildStep> getBuildSteps() {
        return Collections.unmodifiableList(buildSteps);
    }

    @MappedContainer
    public static class BlockToPlace {
        public Vector3i pos;
        public Block block;
    }

    @MappedContainer
    public static class BuildStep {

        public List<BlockToPlace> blocksInStep;

        public BuildStep(List<BlockToPlace> blocksInStep) {
            this.blocksInStep = blocksInStep;
        }

        public BuildStep() {
            this(Collections.emptyList());
        }
    }
}
