// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.ActivationPredicted;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.structureTemplates.components.ProtectedRegionsComponent;
import org.terasology.structureTemplates.internal.components.NoInteractionWhenProtected;
import org.terasology.structureTemplates.util.ProtectedRegionUtility;

import java.util.Collection;
import java.util.Collections;

/**
 * System to make {@link ProtectedRegionsComponent} work.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ProtectedRegionClientSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ProtectedRegionClientSystem.class);

    @In
    private EntityManager entityManager;

    private boolean isInProtectedRegion(Collection<Vector3i> positions) {
        for (EntityRef regionEntity : entityManager.getEntitiesWith(ProtectedRegionsComponent.class)) {
            if (ProtectedRegionUtility.isInProtectedRegion(positions, regionEntity)) {
                return true;
            }
        }
        return false;
    }

    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent(components = NoInteractionWhenProtected.class)
    public void onActivationPredicted(ActivationPredicted event, EntityRef target) {
        Vector3f position = event.getTarget().getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        //TODO: is this equivalent to  new Vector3i(position, RoundingMode.HALF_UP);
        Vector3i roundedPosition = new Vector3i(Math.round(position.x), Math.round(position.y), Math.round(position.z));
        logger.info(roundedPosition + " " + isInProtectedRegion(Collections.singleton(roundedPosition)));
        if (isInProtectedRegion(Collections.singleton(roundedPosition))) {
            event.consume();
        }
    }
}
