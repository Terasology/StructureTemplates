// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.engine.world.block.BlockUri;

/**
 * Gets sent to the server to inform it that the user wants to have a block item from the toolbox.
 */
@ServerEvent
public class BlockFromToolboxRequest implements Event {
    // BlockUri seems not to get serialized properly
    private String blockUri;

    public BlockFromToolboxRequest(BlockUri blockUri) {
        this.blockUri = blockUri.toString();
    }

    public BlockFromToolboxRequest() {
    }

    public BlockUri getBlockUri() {
        return new BlockUri(blockUri);
    }
}
