// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.joml.Vector3i;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: add javadoc
 */
public class BuildStepwiseStructureComponent implements Component<BuildStepwiseStructureComponent> {
    private List<BuildStep> buildSteps;

    public BuildStepwiseStructureComponent(List<BuildStep> buildSteps) {
        this.buildSteps = buildSteps;
    }

    public BuildStepwiseStructureComponent() {
        this.buildSteps = Collections.emptyList();
    }

    public List<BuildStep> getBuildSteps() {
        return Collections.unmodifiableList(buildSteps);
    }

    @Override
    public void copyFrom(BuildStepwiseStructureComponent other) {
        this.buildSteps = other.buildSteps.stream()
                .map(BuildStep::copy)
                .collect(Collectors.toList());
    }

    @MappedContainer
    public static class BlockToPlace {
        public Vector3i pos;
        public Block block;

        BlockToPlace copy() {
            BlockToPlace newBlockToPlace = new BlockToPlace();
            newBlockToPlace.block = this.block;
            newBlockToPlace.pos = new Vector3i(this.pos);
            return newBlockToPlace;
        }
    }

    @MappedContainer
    public static class BuildStep {

        public BuildStep(List<BlockToPlace> blocksInStep) {
            this.blocksInStep = blocksInStep;
        }

        public BuildStep() {
            this(Collections.emptyList());
        }

        public List<BlockToPlace> blocksInStep;

        BuildStep copy() {
            return new BuildStep(blocksInStep.stream().map(BlockToPlace::copy).collect(Collectors.toList()));
        }
    }
}
