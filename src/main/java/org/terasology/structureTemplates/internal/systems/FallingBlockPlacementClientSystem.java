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

import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.structureTemplates.components.FallingBlockComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FallingBlockPlacementClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    /**
     * A strongly rounded gravity value
     */
    public static final float FALLING_BLOCK_ACCELERATION_IN_M_PER_MS = -10f / (1000f * 1000f);

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private AssetManager assetManager;

    private Map<EntityRef, EntityRef> dataToVisualEntityMap = new LinkedHashMap<>();

    @Override
    public void update(float delta) {
        for (Map.Entry<EntityRef, EntityRef> entry: dataToVisualEntityMap.entrySet()) {
            EntityRef dataEntity = entry.getKey();
            EntityRef visualEntity = entry.getValue();

            LocationComponent visualLocationComponent = visualEntity.getComponent(LocationComponent.class);
            Vector3f position = determineVisualFallingBlockPosition(dataEntity);
            visualLocationComponent.setWorldPosition(position);
            visualEntity.saveComponent(visualLocationComponent);
        }
    }

    Vector3f determineVisualFallingBlockPosition(EntityRef dataEntity) {
        FallingBlockComponent component = dataEntity.getComponent(FallingBlockComponent.class);
        LocationComponent dataLocationComponent = dataEntity.getComponent(LocationComponent.class);
        Vector3f finalPosition = dataLocationComponent.getWorldPosition(new Vector3f());
        return determineVisualFallingBlockPosition(component, finalPosition);
    }

    Vector3f determineVisualFallingBlockPosition(FallingBlockComponent component, Vector3f finalPosition) {
        Vector3f position;
        if (time.getGameTimeInMs() > component.stopGameTimeInMs) {
            position = finalPosition;
        } else {
            float totalFallDurationInMs = component.stopGameTimeInMs - component.startGameTimeInMs;
            float totalFallHeight = 0.5f*(-FALLING_BLOCK_ACCELERATION_IN_M_PER_MS) * totalFallDurationInMs * totalFallDurationInMs;

            float fallDuration = (component.startGameTimeInMs - time.getGameTimeInMs());

            float amountFallen = 0.5f*(-FALLING_BLOCK_ACCELERATION_IN_M_PER_MS) * fallDuration * fallDuration;

            float finalY = finalPosition.getY();
            float initialY = finalY + totalFallHeight;
            float newY = initialY - amountFallen;
            position = new Vector3f(finalPosition.getX(), newY, finalPosition.getZ());
        }
        return position;
    }


    @ReceiveEvent
    public void onActivatedFallingBlockComponent(OnActivatedComponent event, EntityRef dataEntity,
                                             FallingBlockComponent component, LocationComponent dataLocationComponent) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(false);
        Block block = blockManager.getBlock(component.blockUri);
        MeshComponent meshComponent = new MeshComponent();
        meshComponent.mesh = block.getMeshGenerator().getStandaloneMesh();
        meshComponent.material = assetManager.getAsset("engine:terrain", Material.class).get();
        meshComponent.translucent = block.isTranslucent();
        entityBuilder.addComponent(meshComponent);
        ;
        LocationComponent locationComponent = new LocationComponent();
        locationComponent.setWorldPosition(determineVisualFallingBlockPosition(dataEntity));
        locationComponent.setWorldScale(0.9999f);
        entityBuilder.addComponent(locationComponent);

        EntityRef visualEntity = entityBuilder.build();
        dataToVisualEntityMap.put(dataEntity, visualEntity);
    }


    @ReceiveEvent
    public void onBeforeDeactivateFallingBlockComponent(BeforeDeactivateComponent event, EntityRef dataEntity,
                                                    FallingBlockComponent component) {
        EntityRef visualEntity = dataToVisualEntityMap.remove(dataEntity);
        if (visualEntity != null) {
            visualEntity.destroy();
        }
    }


}
