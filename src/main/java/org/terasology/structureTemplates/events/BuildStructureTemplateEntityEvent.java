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
package org.terasology.structureTemplates.events;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This event gets sent to the structure template editor entity (the origin block) when
 * a new structure template should be created from blocks that have been marked by that structure template editor.
 */
public class BuildStructureTemplateEntityEvent implements Event {
    private BlockRegionTransform transformToRelative;
    private Map<Block, Set<Vector3i>> blockToAbsolutePositionMap;
    private MutableComponentContainer templateEntity;

    public BuildStructureTemplateEntityEvent() {
    }

    public BuildStructureTemplateEntityEvent(MutableComponentContainer templateEntity, BlockRegionTransform transform,
                                             Map<Block, Set<Vector3i>> blockToRelativePositionMap) {
        this.templateEntity = templateEntity;
        this.transformToRelative = transform;
        this.blockToAbsolutePositionMap = blockToRelativePositionMap;
    }

    public BlockRegionTransform getTransformToRelative() {
        return transformToRelative;
    }

    public MutableComponentContainer getTemplateEntity() {
        return templateEntity;
    }

    public Collection<Vector3i> findAbsolutePositionsOf(BlockFamily blockFamily) {
        List<Vector3i> positions = new ArrayList<>();
        for (Block block: blockFamily.getBlocks()) {
            Collection<Vector3i> positionsOfBlock = blockToAbsolutePositionMap.getOrDefault(block, Collections.emptySet());
            positions.addAll(positionsOfBlock);
        }
        return positions;
    }
}
