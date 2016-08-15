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
package org.terasology.structureTemplates.interfaces;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.structureTemplates.components.StructureTemplateComponent;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;

import java.util.Iterator;

/**
 * Allows you to get structure template prefabs.
 */
public interface StructureTemplateProvider {

    /**
     * @param structureTemplateTypePrefab the urn of a prefab that has the {@link StructureTemplateTypeComponent}.
     *                                    There must be at least 1 prefab with the {@link StructureTemplateComponent}
     *                                    that is referencing that prefab.
     * @return a random structure template prefab that has the given structure template type.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a
     *                                  {@link StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the specified
     *                                  structure tempalte type.
     */
    EntityRef getRandomTemplateOfType(String structureTemplateTypePrefab) throws IllegalArgumentException;


    /**
     * @param structureTemplateTypePrefab prefab that has the {@link StructureTemplateTypeComponent}.
     *                                    There must be at least 1 prefab with the {@link StructureTemplateComponent}
     *                                    that is referencing that prefab.
     * @return a random structure template prefab that has the given structure template type.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a
     *                                  {@link StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the specified
     *                                  structure tempalte type.
     */
    EntityRef getRandomTemplateOfType(Prefab structureTemplateTypePrefab) throws IllegalArgumentException;

    /**
     * @param structureTemplateTypePrefab prefab that has the {@link StructureTemplateTypeComponent}.
     *                                    There must be at least 1 prefab with the {@link StructureTemplateComponent}
     *                                    that is referencing that prefab.
     * @return a iterator that iterates over all structute templates of the specified type in a spawn chance based
     * random order. The higher the spawn chance the more likly a template will come first.
     * @throws IllegalArgumentException if there is no prefab with the given urn that has a
     *                                  {@link StructureTemplateTypeComponent}.
     * @throws IllegalArgumentException if there is no {@link StructureTemplateComponent} that references the specified
     *                                  structure tempalte type.
     */
    public Iterator<EntityRef> iterateStructureTempaltesOfTypeInRandomOrder(Prefab structureTemplateTypePrefab)
            throws IllegalArgumentException;
}
