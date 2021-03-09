// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.engine.logic.clipboard.ClipboardManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIText;

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
    private Consumer<BlockRegion> okHandler;

    @In
    private ClipboardManager clipboardManager;


    @In
    private LocalPlayer localPlayer;


    public BlockRegion getRegion() {
        return new BlockRegion(integerFromField(minXField), integerFromField(minYField), integerFromField(minZField))
                .setSize(integerFromField(sizeXField), integerFromField(sizeYField), integerFromField(sizeZField));
    }

    private Integer integerFromField(UIText field) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setRegion(BlockRegionc region) {
        minXField.setText(Integer.toString(region.minX()));
        minYField.setText(Integer.toString(region.minY()));
        minZField.setText(Integer.toString(region.minZ()));
        sizeXField.setText(Integer.toString(region.getSizeX()));
        sizeYField.setText(Integer.toString(region.getSizeY()));
        sizeZField.setText(Integer.toString(region.getSizeZ()));
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

    public void setOkHandler(Consumer<BlockRegion> okHandler) {
        this.okHandler = okHandler;
    }
}
