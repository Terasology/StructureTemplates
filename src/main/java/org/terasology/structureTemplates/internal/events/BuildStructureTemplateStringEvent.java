// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Send this to a structure template in oder to get a json representation of it.
 * Gets used by the editor.
 */
public class BuildStructureTemplateStringEvent implements Event {
    private Map<Class<? extends Component>, String> map = new LinkedHashMap<>();

    public Map<Class<? extends Component>, String> getMap() {
        return map;
    }

    public void addJsonForComponent(String stringRepresentation, Class<? extends Component> componentClass) {
        map.put(componentClass, stringRepresentation);
    }
}
