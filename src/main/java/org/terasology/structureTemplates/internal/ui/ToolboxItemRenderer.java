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

import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.itemRendering.StringTextIconRenderer;

/**
 * Renders NUI editor nodes along with an icon depending on their types.
 */
public class ToolboxItemRenderer extends StringTextIconRenderer<ToolboxTreeValue> {

    public ToolboxItemRenderer() {
        super(2, 2, 5, 5);
    }

    @Override
    public String getString(ToolboxTreeValue value) {
        return value.toString();
    }

    @Override
    public TextureRegion getTexture(ToolboxTreeValue value) {
        return value.getTextureRegion();
    }
}
