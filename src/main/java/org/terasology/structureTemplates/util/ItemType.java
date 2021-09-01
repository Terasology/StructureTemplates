// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

/**
 * Describes the type of item to create, either a structure template or a structure spawner.
 * <p>
 * Holds information about the associated icon and (display name) suffix.
 */
public enum ItemType {
    TEMPLATE(" Template", "StructureTemplates:StructureTemplateOrigin", "StructureTemplates:StructureTemplateOrigin"),
    SPAWNER(" Spawner", "StructureTemplates:Blueprint32", "StructureTemplates:Blueprint16");

    public final String suffix;
    public final String iconUrn;
    public final String thumbnailUrn;

    ItemType(final String suffix, String iconUrn, String thumbnailUrn) {
        this.suffix = suffix;
        this.iconUrn = iconUrn;
        this.thumbnailUrn = thumbnailUrn;
    }
}
