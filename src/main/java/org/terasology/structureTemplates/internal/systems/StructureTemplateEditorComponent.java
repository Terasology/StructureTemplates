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
package org.terasology.structureTemplates.internal.systems;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;

/**
 * Used to describe an block region location
 */
@ForceBlockActive
public class StructureTemplateEditorComponent implements Component {


    /**
     * Edited region in absolute coordinates
     */
    @Replicate(FieldReplicateType.OWNER_TO_SERVER_TO_CLIENT)
    public Region3i editRegion = Region3i.createBounded(new Vector3i(0,0,0), new Vector3i(0,0,0));

}
