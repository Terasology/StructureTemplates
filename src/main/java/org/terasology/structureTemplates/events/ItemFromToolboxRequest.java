// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.ServerEvent;

/**
 * Gets sent to the server to inform it that the user wants to have a structure template from the toolbox.
 */
@ServerEvent
public class ItemFromToolboxRequest implements Event {
    private Prefab itemPrefab;

    public ItemFromToolboxRequest(Prefab itemPrefab) {
        this.itemPrefab = itemPrefab;
    }

    public ItemFromToolboxRequest() {
    }

    public Prefab getItemPrefab() {
        return itemPrefab;
    }
}
