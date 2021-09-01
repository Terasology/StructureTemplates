// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;

/**
 * The event gets sent to a character entity at the server.
 *
 * The server will then check if the character is interacting with a entity that has the
 * {@link StructureTemplateOriginComponent}.
 *
 * If that is the case that component will be updated with to have only the specified region
 */
@ServerEvent
public class MakeBoxShapedRequest extends NetworkEvent {
    private BlockRegion region = new BlockRegion(BlockRegion.INVALID);
    public MakeBoxShapedRequest() {
    }

    public MakeBoxShapedRequest(BlockRegionc region) {
        this.region.set(region);
    }

    public BlockRegionc getRegion() {
        return region;
    }
}
