// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UILabel;

/**
 * Overlay shown when the user tries to spawn a building but can't because the preconditions are not met.
 */
public class StructurePlacementFailureScreen extends CoreScreenLayer {

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "okButton", button -> getManager().popScreen());
    }

    public void setMessage(String message) {
        UILabel messageLabel = find("message", UILabel.class);
        messageLabel.setText(message);
    }
}
