// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;
import org.terasology.structureTemplates.util.AnimationType;

/**
 * The event gets sent to a character entity at the server.
 *
 * The server will then check if the character is interacting with a entity that has the {@link StructureTemplateComponent}
 * and the {@link StructureTemplateOriginComponent}.
 *
 * If that is the case that component will be updated with the values of the event.
 */
@ServerEvent
public class RequestStructureTemplatePropertiesChange implements Event {
    private Prefab prefab;
    private Integer spawnChance;
    private AnimationType animationType;

    public RequestStructureTemplatePropertiesChange(Prefab prefab, Integer spawnChance, AnimationType animationType) {
        this.prefab = prefab;
        this.spawnChance = spawnChance;
        this.animationType = animationType;
    }

    public RequestStructureTemplatePropertiesChange() {
    }

    public Prefab getPrefab() {
        return prefab;
    }

    public Integer getSpawnChance() {
        return spawnChance;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }
}
