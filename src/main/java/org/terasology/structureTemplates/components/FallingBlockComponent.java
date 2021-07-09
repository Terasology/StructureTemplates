// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Shows a fall animation until it reaches target height.
 * Once there is a block at the target height this entity will be destroyed.
 */

@Replicate(FieldReplicateType.SERVER_TO_CLIENT)
public class FallingBlockComponent implements Component<FallingBlockComponent> {

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long stopGameTimeInMs;

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long startGameTimeInMs;

    /**
     * The block URI as string as it looks like {@link BlockUri} does not get synchronized yet
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public String blockUri;

    @Override
    public void copy(FallingBlockComponent other) {
        this.stopGameTimeInMs = other.stopGameTimeInMs;
        this.startGameTimeInMs = other.startGameTimeInMs;
        this.blockUri = other.blockUri;
    }
}
