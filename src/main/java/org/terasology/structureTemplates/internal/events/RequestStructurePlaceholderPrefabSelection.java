// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.internal.components.StructurePlaceholderComponent;

/**
 * The event gets sent to a character entity at the server.
 * <p>
 * The server will then check if the character is interacting with a entity that has the {@link
 * StructurePlaceholderComponent}.
 * <p>
 * If that is the case that component will be updated with the values of the event.
 */
@ServerEvent
public class RequestStructurePlaceholderPrefabSelection implements Event {
    private Prefab prefab;

    public RequestStructurePlaceholderPrefabSelection(Prefab prefab) {
        this.prefab = prefab;
    }

    public RequestStructurePlaceholderPrefabSelection() {
    }

    public Prefab getPrefab() {
        return prefab;
    }
}
