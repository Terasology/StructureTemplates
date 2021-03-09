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
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockUri;

/**
 * Shows a fall animation until it reaches target height.
 * Once there is a block at the target height this entity will be destroyed.
 */

@Replicate(FieldReplicateType.SERVER_TO_CLIENT)
public class FallingBlockComponent implements Component {

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long stopGameTimeInMs;

    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public long startGameTimeInMs;

    /**
     * The block URI as string as it looks like {@link BlockUri} does not get synchronized yet
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public String blockUri;
}
