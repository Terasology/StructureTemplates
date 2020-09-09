// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.event.Event;

import java.util.LinkedHashMap;

/**
 * Send this to a structure template in oder to get a json representation of it. Gets used by the editor.
 */
public class BuildStructureTemplateStringEvent implements Event {
    private final LinkedHashMap<Class<? extends Component>, String> map = new LinkedHashMap<>();

    public LinkedHashMap<Class<? extends Component>, String> getMap() {
        return map;
    }

    public void addJsonForComponent(String stringRepresentation, Class<? extends Component> componentClass) {
        map.put(componentClass, stringRepresentation);
    }


}
