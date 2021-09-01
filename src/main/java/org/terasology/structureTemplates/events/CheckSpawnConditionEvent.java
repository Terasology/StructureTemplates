// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.structureTemplates.components.CheckBlockRegionConditionComponent;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * Sent to entities with spawn condition components, in order to check if the spawn conditions are met at a certain
 * position. The event handler should call {@link #setPreventSpawn(boolean)} with true and constume the event
 * when spawning is not possible.
 *
 * See {@link CheckBlockRegionConditionComponent} for an example for such a component.
 */
public class CheckSpawnConditionEvent extends AbstractConsumableEvent {
    private BlockRegionTransform blockRegionTransform;
    /** result of the event */
    private boolean preventSpawn;
    /** Meta data about result */
    private Prefab failedSpawnCondition;
    /** Meta data about result */
    private BlockRegion spawnPreventingRegion;

    public CheckSpawnConditionEvent(BlockRegionTransform blockRegionTransform) {
        this.blockRegionTransform = blockRegionTransform;
    }

    public BlockRegionTransform getBlockRegionTransform() {
        return blockRegionTransform;
    }

    /**
     * Handler of this event call this method when they have determined if a spawn is possible or not.
     *
     * This field stores the result of the event. However usually the event gets also consumed when
     * a spawn issue gets found to prevent a later handler to change the result.
     */
    public void setPreventSpawn(boolean preventSpawn) {
        this.preventSpawn = preventSpawn;
    }

    /**
     *
     * @param failedSpawnCondition the entity that prevented the spawning. IT may contain later on an error message
     *                              that can be displayed to the user.
     */
    public void setFailedSpawnCondition(Prefab failedSpawnCondition) {
        this.failedSpawnCondition = failedSpawnCondition;
    }

    public void setSpawnPreventingRegion(BlockRegion spawnPreventingRegion) {
        this.spawnPreventingRegion = spawnPreventingRegion;
    }

    public boolean isPreventSpawn() {
        return preventSpawn;
    }

    public Prefab getFailedSpawnCondition() {
        return failedSpawnCondition;
    }

    public BlockRegion getSpawnPreventingRegion() {
        return spawnPreventingRegion;
    }
}
