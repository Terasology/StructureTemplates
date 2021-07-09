// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.components;

import org.terasology.gestalt.entitysystem.component.EmptyComponent;

/** A marker component for structures that don't need explicit spawning of air blocks. */
// TODO : Generalise for all kind of Blocks
// TODO : Add marker component to toolbox UI
public class IgnoreAirBlocksComponent extends EmptyComponent<IgnoreAirBlocksComponent> {
}
