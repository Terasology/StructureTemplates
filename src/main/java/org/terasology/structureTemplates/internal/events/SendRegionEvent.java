// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.List;

/**
 * Send this to any block Entity within the Structure Template to create a multiBlock entity with the
 */
public class SendRegionEvent implements Event {
    public List<SpawnBlockRegionsComponent.RegionToFill> regions;

    public SendRegionEvent(List<SpawnBlockRegionsComponent.RegionToFill> regions) {
        this.regions = regions;
    }
}
