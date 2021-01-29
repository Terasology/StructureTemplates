/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 * The event gets sent to a character entity at the server.
 *
 * The server will then check if the character is interacting with a structure template editor/origin.
 *
 * If that is the case then it will return the json representation of the structure template.
 *
 * The event gets trigged by a button in the structure template editor.
 *
 */
@ServerEvent
public class StructureTemplateStringRequest extends NetworkEvent {
    public StructureTemplateStringRequest() {
    }

}
