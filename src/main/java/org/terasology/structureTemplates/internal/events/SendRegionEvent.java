// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;

import java.util.List;

@ServerEvent
public class SendRegionEvent implements Event {
    public List<SpawnBlockRegionsComponent.RegionToFill> regions;

    public SendRegionEvent(List<SpawnBlockRegionsComponent.RegionToFill> regions) {
        this.regions = regions;
    }
}
