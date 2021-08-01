// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.recipe;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.multiBlock2.MultiBlockDefinition;
import org.terasology.multiBlock2.recipe.MultiBlockRecipe;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StructureTemplateRecipe<T extends MultiBlockDefinition> implements MultiBlockRecipe<T>  {
    private BlockEntityRegistry blockEntityRegistry;
    private Map<Vector3f, List<SpawnBlockRegionsComponent.RegionToFill>> store = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(StructureTemplateRecipe.class);
    private WorldProvider worldProvider;

    public StructureTemplateRecipe(BlockEntityRegistry blockEntityRegistry) {
        this.blockEntityRegistry = blockEntityRegistry;
    }

    @Override
    public T detectFormingMultiBlock(Vector3ic location) {
        for (Vector3f key : store.keySet()) {
            logger.info(String.valueOf(key.x()));
            logger.info(String.valueOf(key.y()));
            logger.info(String.valueOf(key.z()));
        }
        logger.info(String.valueOf(location.x()));
        logger.info(String.valueOf(location.y()));
        logger.info(String.valueOf(location.z()));
        List<SpawnBlockRegionsComponent.RegionToFill> temp =  store.get(new Vector3f((float) location.x(), (float) location.y(), (float) location.z()));
        return createMultiBlockDefinition(temp, location);
    }


    protected abstract T createMultiBlockDefinition(List<SpawnBlockRegionsComponent.RegionToFill> temp, Vector3ic location);

}
