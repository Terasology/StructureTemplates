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
package org.terasology.structureTemplates.internal.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

/**
 * Gets added to a client entity when that client decides to edit the copy region of a structure template..
 */
public class EditTemplateRegionProcessComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef structureTemplateEditor = EntityRef.NULL;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public boolean recordBlockAddition;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public boolean recordBlockRemoval;
}
