/*
 * Copyright 2017 MovingBlocks
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

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.structureTemplates.components.SpawnPrefabsComponent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;


/**
 * Contains the logic to make {@link SpawnPrefabsComponent} work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnPrefabServerSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onSpawnStructureWithPrefabSpawn(StructureBlocksSpawnedEvent event, EntityRef entity,
                                                SpawnPrefabsComponent component) {
        for (SpawnPrefabsComponent.PrefabToSpawn prefabToSpawn : component.prefabsToSpawn) {
            Vector3i position = event.getTransformation().transformVector3i(prefabToSpawn.position);
            Quaternionf rotation = event.getTransformation().transformRotation(prefabToSpawn.rotation);

            EntityBuilder entityBuilder = entityManager.newBuilder(prefabToSpawn.prefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(new Vector3f(position));
            locationComponent.setWorldRotation(rotation);

            entityBuilder.build();
        }
    }
}
