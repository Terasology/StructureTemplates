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
