// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * Sent from the server to the client with copied data.
 */
@OwnerEvent
public class CopyBlockRegionResultEvent implements Event {
    private String json;

    protected CopyBlockRegionResultEvent() {
        // for serialization
    }

    public CopyBlockRegionResultEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
