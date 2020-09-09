// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;

/**
 * The event gets sent to a character entity at the server.
 * <p>
 * The server will then check if the character is interacting with a entity that has the {@link
 * StructureTemplateOriginComponent}.
 * <p>
 * If that is the case that component will be updated with to have only the specified region
 */
@ServerEvent
public class MakeBoxShapedRequest extends NetworkEvent {
    private Region3i region;

    public MakeBoxShapedRequest() {
    }

    public MakeBoxShapedRequest(Region3i region) {
        this.region = region;
    }

    public Region3i getRegion() {
        return region;
    }
}
