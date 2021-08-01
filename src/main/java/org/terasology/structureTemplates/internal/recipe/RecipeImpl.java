// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.recipe;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.definition.StructureBlockDefinition;

import java.util.ArrayList;
import java.util.List;

public class RecipeImpl extends StructureTemplateRecipe<StructureBlockDefinition> {

    private static final Logger logger = LoggerFactory.getLogger(RecipeImpl.class);

    public RecipeImpl(BlockEntityRegistry blockEntityRegistry) {
        super(blockEntityRegistry);
    }

    protected StructureBlockDefinition createMultiBlockDefinition(List<SpawnBlockRegionsComponent.RegionToFill> temp, Vector3ic location) {
        ArrayList<Vector3i> blocks = new ArrayList<>();
        logger.info("Test");
        for (SpawnBlockRegionsComponent.RegionToFill region : temp) {
            for (Vector3ic pos : region.region) {
                blocks.add(new Vector3i(pos));
                logger.info(pos.toString());
            }
        }
        return new StructureBlockDefinition(blocks, location, "Structure");
    }

}
