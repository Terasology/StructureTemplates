// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

/**
 * The event gets sent to a character entity at the server.
 * <p>
 * The server will then check if the character is interacting with a structure template editor/origin.
 * <p>
 * If that is the case then it will return the json representation of the structure template.
 * <p>
 * The event gets trigged by a button in the structure template editor.
 */
@ServerEvent
public class StructureTemplateStringRequest extends NetworkEvent {
    public StructureTemplateStringRequest() {
    }

}
