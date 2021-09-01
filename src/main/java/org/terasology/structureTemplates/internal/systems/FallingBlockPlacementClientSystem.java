// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.structureTemplates.components.FallingBlockComponent;

import java.util.LinkedHashMap;
import java.util.Map;

@RegisterSystem(RegisterMode.CLIENT)
public class FallingBlockPlacementClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    /**
     * A strongly rounded gravity value
     */
    public static final float FALLING_BLOCK_ACCELERATION_IN_M_PER_MS = -10f / (1000f * 100f);

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
            Vector3fc position = determineVisualFallingBlockPosition(dataEntity);
            visualLocationComponent.setWorldPosition(position);
            visualEntity.saveComponent(visualLocationComponent);
        }
    }

    private Vector3fc determineVisualFallingBlockPosition(EntityRef dataEntity) {
        FallingBlockComponent component = dataEntity.getComponent(FallingBlockComponent.class);
        LocationComponent dataLocationComponent = dataEntity.getComponent(LocationComponent.class);
        Vector3f finalPosition = dataLocationComponent.getWorldPosition(new Vector3f());

        if (time.getGameTimeInMs() < component.stopGameTimeInMs) {
            float totalFallDurationInMs = component.stopGameTimeInMs - component.startGameTimeInMs;
            float totalFallHeight =
                0.5f * (-FALLING_BLOCK_ACCELERATION_IN_M_PER_MS) * totalFallDurationInMs * totalFallDurationInMs;

            float fallDuration = (component.startGameTimeInMs - time.getGameTimeInMs());

            float amountFallen = 0.5f * (-FALLING_BLOCK_ACCELERATION_IN_M_PER_MS) * fallDuration * fallDuration;

            float finalY = finalPosition.y();
            float initialY = finalY + totalFallHeight;
            finalPosition.y = initialY - amountFallen;
        }
        return finalPosition;
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
