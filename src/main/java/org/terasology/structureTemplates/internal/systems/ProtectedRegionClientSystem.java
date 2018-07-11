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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.ActivationPredicted;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
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

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL, components = {NoInteractionWhenProtected.class})
    public void onActivationPredicted(ActivationPredicted event, EntityRef target) {
        Vector3f position = event.getTarget().getComponent(LocationComponent.class).getWorldPosition();
        Vector3i roundedPosition = new Vector3i(Math.round(position.x), Math.round(position.y), Math.round(position.z));
        logger.info(roundedPosition + " " + isInProtectedRegion(Collections.singleton(roundedPosition)));
        if (isInProtectedRegion(Collections.singleton(roundedPosition))) {
            event.consume();
        }
    }
}
