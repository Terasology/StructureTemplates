// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.definition;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.multiBlock2.MultiBlockDefinition;

import java.util.ArrayList;
import java.util.Collection;

public class StructureBlockDefinition implements MultiBlockDefinition {
    private ArrayList<Vector3i> temp;
    private Vector3ic location;
    private String type;

    public StructureBlockDefinition(ArrayList<Vector3i> temp, Vector3ic location, String type) {
        this.temp = temp;
        this.location = location;
        this.type = type;
    }


    @Override
    public String getMultiBlockType() {
        return type;
    }

    @Override
    public Vector3i getMainBlock() {
        return new Vector3i(location);
    }

    @Override
    public Collection<Vector3i> getMemberBlocks() {
        return temp;
    }

}
