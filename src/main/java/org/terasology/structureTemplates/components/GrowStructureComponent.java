// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;


import org.terasology.gestalt.entitysystem.component.EmptyComponent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

/**
 * Rises a block structure from the ground up when an entity with this component receives a {@link SpawnStructureEvent} and it also has a
 * {@link SpawnBlockRegionsComponent}.
 */
public class GrowStructureComponent extends EmptyComponent<GrowStructureComponent> {
}
