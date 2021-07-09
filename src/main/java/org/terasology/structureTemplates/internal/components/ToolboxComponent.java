// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.components;

import org.terasology.gestalt.entitysystem.component.EmptyComponent;

/**
 * Component that marks a entity as beeing a toolbox.
 *
 * Used by event handlers to verify that an event receiving entity is indeed a toolbox.
 */
public class ToolboxComponent extends EmptyComponent<ToolboxComponent> {
}
