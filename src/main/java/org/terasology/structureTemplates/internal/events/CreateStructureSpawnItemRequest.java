// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

/**
 * The event gets sent to a character entity when the user has requested thh creatin of a structure spawn item in
 * the structure template editor UI that shows up when the character interacts with the structure template origin block.
 */
@ServerEvent
public class CreateStructureSpawnItemRequest extends NetworkEvent {

    public CreateStructureSpawnItemRequest() {
    }

}
