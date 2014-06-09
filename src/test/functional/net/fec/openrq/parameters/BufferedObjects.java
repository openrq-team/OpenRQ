/*
 * Copyright 2014 Jose Lopes
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fec.openrq.parameters;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Used by parameter bounds tests.
 * 
 * @param <T>
 */
final class BufferedObjects<T> implements Iterable<Object[]> {

    static final int DEFAULT_BUFFER_SIZE = 8192;

    private final List<Object[]> internal;
    private final int bufferSize;


    BufferedObjects() {

        this(DEFAULT_BUFFER_SIZE);
    }

    BufferedObjects(int bufferSize) {

        if (bufferSize < 1) throw new IllegalArgumentException("buffer size must be positive");

        this.internal = new ArrayList<>();
        this.bufferSize = bufferSize;
    }

    void add(T obj) {

        if (internal.isEmpty()) {
            addInNewList(obj);
        }
        else {
            final List<T> last = lastList();
            if (last.size() < bufferSize) {
                last.add(obj);
            }
            else {
                addInNewList(obj);
            }
        }
    }

    private List<T> lastList() {

        @SuppressWarnings("unchecked")
        final List<T> lastList = (List<T>)internal.get(internal.size() - 1)[0];
        return lastList;
    }

    private void addInNewList(T obj) {

        final List<T> newList = new ArrayList<>();
        newList.add(obj);
        internal.add(new Object[] {newList});
    }

    @Override
    public Iterator<Object[]> iterator() {

        return internal.iterator();
    }
}
