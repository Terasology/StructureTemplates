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

import org.terasology.entitySystem.event.Event;
import org.terasology.rendering.assets.texture.TextureRegion;

import java.util.function.Supplier;

/**
 *
 */
public final class ToolboxTreeValue {
    private String text;
    private TextureRegion textureRegion;
    private Supplier<Event> itemRequestFactory;

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
    public final String toString() {
        return text;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
}
