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
package org.terasology.structureTemplates.internal.ui;

import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * Overlay shown when the user tries to spawn a building but can't because the preconditions are not met.
 */
public class StructurePlacementFailureScreen extends CoreScreenLayer {

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "okButton",  button -> getManager().popScreen());
    }

    public void setMessage(String message) {
        UILabel messageLabel = find("message", UILabel.class);
        messageLabel.setText(message);
    }
}