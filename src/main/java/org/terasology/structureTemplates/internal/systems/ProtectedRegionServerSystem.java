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
package org.terasology.structureTemplates.internal.systems;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.ActivationPredicted;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.ProtectRegionsForAFewHoursComponent;
import org.terasology.structureTemplates.components.ProtectedRegionsComponent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.internal.components.NoInteractionWhenProtected;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;

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
    public void onAttackEntity(AttackEvent event, EntityRef targetEntity, BlockComponent blockComponent) {
        Vector3i pos = blockComponent.getPosition();

        if (isInProtectedRegion(Collections.singleton(pos))) {
            event.consume();
        }
    }

    private boolean isInProtectedRegion(Collection<Vector3i> positions) {

        for (EntityRef entity : entityManager.getEntitiesWith(ProtectedRegionsComponent.class)) {
            ProtectedRegionsComponent protectedRegionsComponent = entity.getComponent(ProtectedRegionsComponent.class);
            List<Region3i> protectedRegions = protectedRegionsComponent.regions;
            if (protectedRegions != null) {
                for (Region3i region : protectedRegions) {
                    for (Vector3i position : positions) {
                        if (region.encompasses(position)) {
                            return true;
                        }
                    }
                }
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
    public void onActivationPredicted(ActivationPredicted event, EntityRef target) {
        Vector3f position = event.getTarget().getComponent(LocationComponent.class).getWorldPosition();
        Vector3i roundedPosition = new Vector3i(Math.round(position.x), Math.round(position.y), Math.round(position.z));
        if (isInProtectedRegion(Collections.singleton(roundedPosition))) {
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
