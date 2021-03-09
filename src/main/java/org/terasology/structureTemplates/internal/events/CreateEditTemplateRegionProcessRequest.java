/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
