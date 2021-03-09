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

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;

/**
 * Gives a "wall adder" item the ability to do it's work.
 */
public class ReplaceWallItemComponent implements Component {

    /**
     * A {@link BlockUri} transfered as string as transferring block uris is not supported yet.
     */
    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public String blockUri = BlockManager.AIR_ID.toString();

    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public ReplacementType replacementType = ReplacementType.WALL;

    public enum ReplacementType {
        WALL, AIR_INFRONT_OF_WALL, WALL_WITH_BORDER
    }
}
