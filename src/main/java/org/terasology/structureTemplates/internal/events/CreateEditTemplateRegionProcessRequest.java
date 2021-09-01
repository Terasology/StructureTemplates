// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.structureTemplates.internal.components.EditTemplateRegionProcessComponent;
import org.terasology.structureTemplates.internal.components.EditingUserComponent;

/**
 * When the character entity of a client is interacting with a structure template origin block this event can be sent.
 * The event gets sent to the character entity by the client. The server handles the event by creating a entity
 * that represents the editing process of the structure template by the owner of the player. This edit process entity
 * will be assigned to the client entity of the character via a  {@link EditingUserComponent}.
 *
 * The editing process is represented by a entity with the {@link EditTemplateRegionProcessComponent}.
 *
 * If there was already an active editing process entity that editing process will be destroyed.
 *
 */
@ServerEvent
public class CreateEditTemplateRegionProcessRequest implements Event {
    private boolean recordBlockAddition;
    private boolean recordBlockRemoval;

    public CreateEditTemplateRegionProcessRequest(boolean recordBlockAddition, boolean recordBlockRemoval) {
        this.recordBlockAddition = recordBlockAddition;
        this.recordBlockRemoval = recordBlockRemoval;
    }

    public CreateEditTemplateRegionProcessRequest() {

    }

    public boolean isRecordBlockAddition() {
        return recordBlockAddition;
    }

    public boolean isRecordBlockRemoval() {
        return recordBlockRemoval;
    }
}
