// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.ActivationRequestDenied;
import org.terasology.engine.logic.characters.events.AttackEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.common.lifespan.LifespanComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.components.ProtectRegionsForAFewHoursComponent;
import org.terasology.structureTemplates.components.ProtectedRegionsComponent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.internal.components.NoInteractionWhenProtected;
import org.terasology.structureTemplates.util.ProtectedRegionUtility;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * System to make {@link ProtectedRegionsComponent} work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ProtectedRegionServerSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ProtectedRegionServerSystem.class);

    @In
    private EntityManager entityManager;

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onAttackBlock(AttackEvent event, EntityRef targetEntity, BlockComponent blockComponent) {
        Vector3i pos = blockComponent.getPosition();

        if (isInProtectedRegion(Collections.singleton(pos))) {
            event.consume();
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onAttackBlockRegion(AttackEvent event, EntityRef targetEntity,
                                    BlockRegionComponent blockRegionComponent) {
        List<Vector3i> positions = Lists.newArrayList();
        for (Vector3i pos : blockRegionComponent.region) {
            positions.add(pos);
        }

        if (isInProtectedRegion(positions)) {
            event.consume();
        }
    }

    private boolean isInProtectedRegion(Collection<Vector3i> positions) {
        for (EntityRef regionEntity : entityManager.getEntitiesWith(ProtectedRegionsComponent.class)) {
            if (ProtectedRegionUtility.isInProtectedRegion(positions, regionEntity)) {
                return true;
            }
        }
        return false;
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onPlaceBlocks(PlaceBlocks event, EntityRef entity) {
        EntityRef instigator = event.getInstigator();
        EntityRef player = instigator.getOwner();
        if (!player.hasComponent(ClientComponent.class)) {
            return;
        }
        if (isInProtectedRegion(event.getBlocks().keySet())) {
            event.consume();
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL, components = {NoInteractionWhenProtected.class})
    public void onActivation(ActivateEvent event, EntityRef target) {
        Vector3f position = event.getTarget().getComponent(LocationComponent.class).getWorldPosition();
        Vector3i roundedPosition = new Vector3i(Math.round(position.x), Math.round(position.y), Math.round(position.z));
        if (isInProtectedRegion(Collections.singleton(roundedPosition))) {
            event.getInstigator().send(new ActivationRequestDenied(event.getActivationId()));
            event.consume();
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onStructureBlocksSpawnedEvent(StructureBlocksSpawnedEvent event, EntityRef entity,
                                              ProtectRegionsForAFewHoursComponent component) {
        EntityBuilder entityBuilder = entityManager.newBuilder();
        entityBuilder.setPersistent(true);
        entityBuilder.addOrSaveComponent(component);
        List<Region3i> absoluteRegions = Lists.newArrayList();
        for (Region3i relativeRegion : component.regions) {
            absoluteRegions.add(event.getTransformation().transformRegion(relativeRegion));
        }
        ProtectedRegionsComponent protectedRegionsComponent = new ProtectedRegionsComponent();
        protectedRegionsComponent.regions = absoluteRegions;
        entityBuilder.addOrSaveComponent(protectedRegionsComponent);

        LifespanComponent lifespanComponent = new LifespanComponent();
        lifespanComponent.lifespan = component.hoursToProtect * 3600;
        entityBuilder.addOrSaveComponent(lifespanComponent);
        entityBuilder.build();
    }

}
