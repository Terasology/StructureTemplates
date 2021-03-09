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

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.structureTemplates.internal.components.StructureTemplateOriginComponent;

/**
 * The event gets sent to a character entity at the server.
 *
 * The server will then check if the character is interacting with a entity that has the
 * {@link StructureTemplateOriginComponent}.
 *
 * If that is the case that component will be updated with to have only the specified region
 */
@ServerEvent
public class MakeBoxShapedRequest extends NetworkEvent {
    private BlockRegion region = new BlockRegion(BlockRegion.INVALID);
    public MakeBoxShapedRequest() {
    }

    public MakeBoxShapedRequest(BlockRegionc region) {
        this.region.set(region);
    }

    public BlockRegionc getRegion() {
        return region;
    }
}
