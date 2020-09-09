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
public class StructureTemplateFromToolboxRequest implements Event {
    private Prefab structureTemplatePrefab;

    public StructureTemplateFromToolboxRequest(Prefab structureTemplatePrefab) {
        this.structureTemplatePrefab = structureTemplatePrefab;
    }

    public StructureTemplateFromToolboxRequest() {
    }

    public Prefab getStructureTemplatePrefab() {
        return structureTemplatePrefab;
    }
}
