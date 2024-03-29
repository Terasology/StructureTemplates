// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.function.Supplier;

public final class ToolboxTreeValue {
    private final String text;
    private final TextureRegion textureRegion;
    private final Supplier<Event> itemRequestFactory;

    public ToolboxTreeValue(String text, TextureRegion textureRegion, Supplier<Event> itemRequestFactory) {
        this.text = text;
        this.textureRegion = textureRegion;
        this.itemRequestFactory = itemRequestFactory;
    }

    public String getText() {
        return text;
    }

    public Supplier<Event> getItemRequestFactory() {
        return itemRequestFactory;
    }

    @Override
    public String toString() {
        return text;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
}
