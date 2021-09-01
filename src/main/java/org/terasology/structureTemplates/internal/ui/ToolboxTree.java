// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.internal.ui;

import org.terasology.nui.widgets.treeView.Tree;

public final class ToolboxTree extends Tree<ToolboxTreeValue> {
    public ToolboxTree() {
    }

    public ToolboxTree(ToolboxTreeValue value) {
        setValue(value);
    }

    @Override
    public void addChild(ToolboxTreeValue childValue) {
        addChild(new ToolboxTree(childValue));
    }

    @Override
    public Tree<ToolboxTreeValue> copy() {
        throw new UnsupportedOperationException();
    }


}
