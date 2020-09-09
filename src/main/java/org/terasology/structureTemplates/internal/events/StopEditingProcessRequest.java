// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

/**
 * Sent to a client entity to stop the current editing process. (The entity that represents it will be destroyed)
 * <p>
 * See also {@link CreateEditTemplateRegionProcessRequest}.
 */
@ServerEvent
public class StopEditingProcessRequest implements Event {
}
