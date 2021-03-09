/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.structureTemplates.internal.components;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.Block;
import org.terasology.reflection.MappedContainer;

import java.util.Collections;
import java.util.List;

/**
 * TODO: add javadoc
 */
public class BuildStepwiseStructureComponent implements Component {
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

    @MappedContainer
    public static class BlockToPlace {
        public Vector3i pos;
        public Block block;
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
    }
}
