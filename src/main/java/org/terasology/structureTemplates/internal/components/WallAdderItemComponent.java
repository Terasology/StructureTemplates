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

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.List;

/**
 * Gives a "wall adder" item the ability to do it's work.
 */
public class WallAdderItemComponent implements Component {
    // TODO it would be saver to let the server calculate the regions
    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public List<Region3i> wallRegions;

    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public BlockUri blockUri = BlockManager.AIR_ID;
}