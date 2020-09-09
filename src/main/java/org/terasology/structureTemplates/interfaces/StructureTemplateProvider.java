// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.interfaces;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;

import java.util.Iterator;

/**
 * Allows you to get structure template prefabs.
 */
public interface StructureTemplateProvider {

    /**
     * @param structureTemplateTypePrefab the urn of a prefab that has the {@link
     *         StructureTemplateTypeComponent}. There must be at least 1 prefab with the {@link
     *         StructureTemplateComponent} that is referencing that prefab.
     * @return a random structure template prefab that has the given structure template type.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a {@link
     *         StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the
     *         specified structure tempalte type.
     */
    EntityRef getRandomTemplateOfType(String structureTemplateTypePrefab) throws IllegalArgumentException;


    /**
     * @param structureTemplateTypePrefab prefab that has the {@link StructureTemplateTypeComponent}. There must
     *         be at least 1 prefab with the {@link StructureTemplateComponent} that is referencing that prefab.
     * @return a random structure template prefab that has the given structure template type.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a {@link
     *         StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the
     *         specified structure tempalte type.
     */
    EntityRef getRandomTemplateOfType(Prefab structureTemplateTypePrefab) throws IllegalArgumentException;

    /**
     * @param structureTemplateTypePrefab prefab that has the {@link StructureTemplateTypeComponent}. There must
     *         be at least 1 prefab with the {@link StructureTemplateComponent} that is referencing that prefab.
     * @return a iterator that iterates over all structute templates of the specified type in a spawn chance based
     *         random order. The higher the spawn chance the more likly a template will come first.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a {@link
     *         StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the
     *         specified structure tempalte type.
     */
    Iterator<EntityRef> iterateStructureTempaltesOfTypeInRandomOrder(Prefab structureTemplateTypePrefab)
            throws IllegalArgumentException;
}
