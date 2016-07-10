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

import org.terasology.entitySystem.Component;
import org.terasology.logic.common.DisplayNameComponent;

/**
 * Only used as marker component to allow the search for prefabs that describe structure template types.
 *
 * Structure template type prefabs get referenced by {@link ScheduleStructurePlacementComponent}s and
 * {@link StructureTemplateComponent}s. Via this reference a link between the 2 is created.
 *
 * The prefab with the {@link StructureTemplateComponent} describes the structure type that the entity with
 * {@link StructureTemplateComponent} must devliper and that the component {@link ScheduleStructurePlacementComponent}
 * can exspect. So prefabs with {@link StructureTemplateTypeComponent}s are acting like a interface constract.
 *
 * Prefabs with the {@link StructureTemplateTypeComponent} should also have a {@link DisplayNameComponent} so
 * that they can be listed with proper name and description in editor UIs.
 */
public class StructureTemplateTypeComponent implements Component {
}
