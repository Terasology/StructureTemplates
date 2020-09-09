// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockUri;

/**
 * Shows a fall animation until it reaches target height. Once there is a block at the target height this entity will be
 * destroyed.
 */

@Replicate(FieldReplicateType.SERVER_TO_CLIENT)
public class FallingBlockComponent implements Component {

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long stopGameTimeInMs;

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long startGameTimeInMs;

    /**
     * The block URI as string as it looks like {@link BlockUri} does not get synchronized yet
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public String blockUri;
}
