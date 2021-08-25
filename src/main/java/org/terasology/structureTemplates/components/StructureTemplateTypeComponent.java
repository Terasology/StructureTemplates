// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.gestalt.entitysystem.component.EmptyComponent;

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
public class StructureTemplateTypeComponent extends EmptyComponent<StructureTemplateTypeComponent> {
}
