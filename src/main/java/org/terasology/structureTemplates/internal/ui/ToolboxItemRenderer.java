// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.JomlUtil;
import org.joml.Vector2i;
import org.terasology.nui.asset.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.nui.Canvas;
import org.terasology.nui.TextLineBuilder;
import org.terasology.nui.itemRendering.AbstractItemRenderer;
import org.terasology.nui.itemRendering.StringTextIconRenderer;

import java.util.List;

/**
 * Renders NUI editor nodes along with an icon depending on their types.
 *
 * Does not extend {@link StringTextIconRenderer} as it does not allow for TextureRegions yet.
 */
public class ToolboxItemRenderer extends AbstractItemRenderer<ToolboxTreeValue> {

    private final int marginTop;
    private final int marginBottom;
    private final int marginLeft;
    private final int marginRight;

    public ToolboxItemRenderer() {
        this(2, 2, 5, 5);
    }

    public ToolboxItemRenderer(int marginTop, int marginBottom, int marginLeft, int marginRight) {
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    @Override
    public void draw(ToolboxTreeValue value, Canvas canvas) {
        // Drawing the icon
        TextureRegion texture = getTexture(value);

        if (texture != null) {
            if (marginTop + texture.getHeight() + marginBottom > canvas.size().y) {
                // Icon does not fit within the canvas - vertically shrinking it
                int iconHeight = canvas.size().y - marginTop - marginBottom;
                canvas.drawTexture(texture, JomlUtil.rectangleiFromMinAndSize(marginLeft, marginTop, texture.getWidth(), iconHeight));
            } else {
                // Icon fits within the canvas - vertically centering it
                int iconVerticalPosition = (canvas.size().y - texture.getHeight()) / 2;
                canvas.drawTexture(texture, JomlUtil.rectangleiFromMinAndSize(marginLeft, iconVerticalPosition, texture.getWidth(), texture.getHeight()));
            }
        }

        // Drawing the text, adjusting for icon width
        String text = getString(value);

        int iconWidth;
        if (texture != null) {
            iconWidth = marginLeft + texture.getWidth() + marginRight;
        } else {
            iconWidth = 0;
        }

        Rectanglei textRegion = JomlUtil.rectangleiFromMinAndSize(iconWidth, 0, canvas.getRegion().lengthX() - iconWidth, canvas.getRegion().lengthY());
        canvas.drawText(text, textRegion);
    }


    @Override
    public Vector2i getPreferredSize(ToolboxTreeValue value, Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        String text = getString(value);

        TextureRegion texture = getTexture(value);
        if (texture == null) {
            List<String> lines = TextLineBuilder.getLines(font, text, canvas.size().x);
            return font.getSize(lines);
        } else {
            int iconWidth = marginLeft + texture.getWidth() + marginRight;
            List<String> lines = TextLineBuilder.getLines(font, text, canvas.size().x - iconWidth);
            return font.getSize(lines).add(iconWidth, 0);
        }
    }

    public String getString(ToolboxTreeValue value) {
        return value.toString();
    }

    public TextureRegion getTexture(ToolboxTreeValue value) {
        return value.getTextureRegion();
    }
}
