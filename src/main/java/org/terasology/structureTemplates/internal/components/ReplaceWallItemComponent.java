// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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