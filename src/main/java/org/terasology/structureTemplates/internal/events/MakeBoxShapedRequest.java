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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 * Sent by UI to server when the user clicks on the "Make box shaped" button in structure template editor.
 */
@ServerEvent
public class MakeBoxShapedRequest extends NetworkEvent {
    private Region3i region;
    public MakeBoxShapedRequest() {
    }

    public MakeBoxShapedRequest(EntityRef instigator, Region3i region) {
        super(instigator);
        this.region = region;
    }

    public Region3i getRegion() {
        return region;
    }
}
