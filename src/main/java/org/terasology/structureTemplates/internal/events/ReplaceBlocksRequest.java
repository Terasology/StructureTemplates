// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.internal.components.ReplaceWallItemComponent;

/**
 * Sent to an item with the {@link ReplaceWallItemComponent} component when the user requests a block replacement with
 * that item.
 */
@ServerEvent
public class ReplaceBlocksRequest extends NetworkEvent {
    public ReplaceBlocksRequest() {
    }
}
