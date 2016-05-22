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
package org.terasology.structureTemplates.internal.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent.RegionToFill;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

/**
 * Spawns structures when entities with certain components receive a {@link SpawnStructureEvent}.
 * e.g. the entity that receives a {@link SpawnStructureEvent} has a {@link SpawnBlockRegionsComponent} then
 * the regions specified by that component will be filled with the specified block types.s
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class StructureSpawnServerSystem extends BaseComponentSystem {

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onSpawnBlockRegions(SpawnStructureEvent event, EntityRef entity,
                                 SpawnBlockRegionsComponent spawnBlockRegionComponent) {
        BlockRegionTransform transformation = event.getTransformation();
        for (RegionToFill regionToFill: spawnBlockRegionComponent.regionsToFill) {
            Block block = blockManager.getBlock(regionToFill.blockType);

            Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);

            for (Vector3i pos : region) {
                worldProvider.setBlock(pos, block);
            }
        }
    }

}
