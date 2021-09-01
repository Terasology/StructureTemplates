// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.structureTemplates.util;

import java.util.List;

/**
 * Class with utility for iterating lists.
 */
public class ListUtil {
    public static <E> void visitList(List<E> list, ListVisitor<E> visitor) {
        int lastIndex = list.size() -1;
        for (int i = 0; i < list.size(); i++) {
            boolean last = (i == lastIndex);
            visitor.visit(list.get(i), last);
        }
    }
    @FunctionalInterface
    public interface ListVisitor<T> {

        public void visit(T item, boolean last);
    }

}
