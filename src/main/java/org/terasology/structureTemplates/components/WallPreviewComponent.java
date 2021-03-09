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
package org.terasology.structureTemplates.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.structureTemplates.internal.components.ReplaceWallItemComponent;

import java.util.List;

/**
 * This component shows a preview of the the wall taht will be placed by items with the {@link
 * ReplaceWallItemComponent}.
 * <p>
 * It is a separte component so that it is possibly to listen for actual modifications of the {@link
 * ReplaceWallItemComponent}.
 */
public class WallPreviewComponent implements Component {
    // TODO it would be saver to let the server calculate the regions
    @Replicate(FieldReplicateType.OWNER_TO_SERVER)
    public List<BlockRegion> wallRegions;

}
