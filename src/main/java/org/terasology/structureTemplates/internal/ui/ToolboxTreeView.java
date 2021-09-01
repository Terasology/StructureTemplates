// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.nui.widgets.UITreeView;

/**
 *
 */
public class ToolboxTreeView extends UITreeView<ToolboxTreeValue> {


    public ToolboxTreeView() {
        super();
        setItemRenderer(new ToolboxItemRenderer());
    }

    public ToolboxTreeView(String id) {
        super(id);
        setItemRenderer(new ToolboxItemRenderer());
    }
}
