// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.event.Event;
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
