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
package org.terasology.structureTemplates.internal.components;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.ForceBlockActive;

import java.util.List;

/**
 * Used to describe an block region location
 */
@ForceBlockActive
public class StructureTemplateOriginComponent implements Component {


    /**
     * Edited regions in absolute coordinates
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public List<BlockRegion> absoluteTemplateRegions = Lists.newArrayList();

}
