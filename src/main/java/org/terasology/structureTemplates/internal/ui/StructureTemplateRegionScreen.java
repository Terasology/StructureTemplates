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

import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.function.Consumer;

/**
 * Dialog for setting a region
 */
public class StructureTemplateRegionScreen extends CoreScreenLayer {

    private UIText minXField;
    private UIText minYField;
    private UIText minZField;
    private UIText sizeXField;
    private UIText sizeYField;
    private UIText sizeZField;
    private UIButton okButton;
    private UIButton cancelButton;
    private Consumer<Region3i> okHandler;

    @In
    private ClipboardManager clipboardManager;


    @In
    private LocalPlayer localPlayer;


    public Region3i getRegion() {
        return Region3i.createFromMinAndSize(
                new Vector3i(integerFromField(minXField), integerFromField(minYField), integerFromField(minZField)),
                new Vector3i(integerFromField(sizeXField), integerFromField(sizeYField), integerFromField(sizeZField))
        );
    }

    private Integer integerFromField(UIText field) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setRegion(Region3i region) {
        minXField.setText(Integer.toString(region.minX()));
        minYField.setText(Integer.toString(region.minY()));
        minZField.setText(Integer.toString(region.minZ()));
        sizeXField.setText(Integer.toString(region.sizeX()));
        sizeYField.setText(Integer.toString(region.sizeY()));
        sizeZField.setText(Integer.toString(region.sizeZ()));
    }

    @Override
    public void initialise() {
        minXField = find("minXField", UIText.class);
        minYField = find("minYField", UIText.class);
        minZField = find("minZField", UIText.class);

        sizeXField = find("sizeXField", UIText.class);
        sizeYField = find("sizeYField", UIText.class);
        sizeZField = find("sizeZField", UIText.class);


        okButton = find("okButton", UIButton.class);
        if (okButton != null) {
            okButton.subscribe(this::onOkButton);
        }

        cancelButton = find("cancelButton", UIButton.class);
        if (cancelButton != null) {
            cancelButton.subscribe(this::onCancelButton);
        }

    }

    private void onOkButton(UIWidget button) {
        getManager().popScreen();
        okHandler.accept(getRegion());
    }

    private void onCancelButton(UIWidget button) {
        getManager().popScreen();
    }

    public void setOkHandler(Consumer<Region3i> okHandler) {
        this.okHandler = okHandler;
    }
}
