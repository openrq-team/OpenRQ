/*
 * Copyright 2014 OpenRQ Team
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

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 */
package net.fec.openrq.util.linearalgebra.io;


import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;


abstract class CursorIterator implements Iterator<Byte> {

    private enum IteratorState {
        TAKEN_FROM_THESE,
        TAKEN_FROM_THOSE,
        THESE_ARE_EMPTY,
        THOSE_ARE_EMPTY,
    }


    private static final Collection<IteratorState> TAKEN_FROM_BOTH = Arrays.asList(
        IteratorState.TAKEN_FROM_THESE,
        IteratorState.TAKEN_FROM_THOSE
        );


    public abstract byte get();

    public abstract void set(byte value);

    protected abstract int cursor();

    protected int innerCursor() {

        return cursor();
    }

    protected CursorIterator orElse(final CursorIterator those, final JoinFunction function) {

        final CursorIterator these = this;
        return new CursorIterator() {

            private EnumSet<IteratorState> state = EnumSet.copyOf(TAKEN_FROM_BOTH);


            @Override
            public int cursor() {

                if (state.contains(IteratorState.TAKEN_FROM_THESE)) {
                    return these.cursor();
                }
                else {
                    return those.cursor();
                }
            }

            @Override
            public byte get() {

                if (state.contains(IteratorState.TAKEN_FROM_THESE) &&
                    state.contains(IteratorState.TAKEN_FROM_THOSE)) {

                    return function.evaluate(these.get(), those.get());
                }
                else if (state.contains(IteratorState.TAKEN_FROM_THESE)) {
                    return function.evaluate(these.get(), (byte)0);
                }
                else {
                    return function.evaluate((byte)0, those.get());
                }
            }

            @Override
            public void set(byte value) {

                throw new UnsupportedOperationException("Composed iterators are read-only for now.");
            }

            @Override
            public boolean hasNext() {

                if (these.hasNext() || those.hasNext()) {
                    return true;
                }
                if (state.containsAll(TAKEN_FROM_BOTH)) {
                    return false;
                }
                return !state.contains(IteratorState.THESE_ARE_EMPTY) ||
                       !state.contains(IteratorState.THOSE_ARE_EMPTY);
            }

            @Override
            public Byte next() {

                if (state.contains(IteratorState.TAKEN_FROM_THESE)) {
                    if (these.hasNext()) {
                        these.next();
                    }
                    else {
                        state.add(IteratorState.THESE_ARE_EMPTY);
                    }
                }

                if (state.contains(IteratorState.TAKEN_FROM_THOSE)) {
                    if (those.hasNext()) {
                        those.next();
                    }
                    else {
                        state.add(IteratorState.THOSE_ARE_EMPTY);
                    }
                }

                state.remove(IteratorState.TAKEN_FROM_THESE);
                state.remove(IteratorState.TAKEN_FROM_THOSE);

                if (!state.contains(IteratorState.THESE_ARE_EMPTY) &&
                    !state.contains(IteratorState.THOSE_ARE_EMPTY)) {

                    if (these.cursor() < those.cursor()) {
                        state.add(IteratorState.TAKEN_FROM_THESE);
                    }
                    else if (these.cursor() > those.cursor()) {
                        state.add(IteratorState.TAKEN_FROM_THOSE);
                    }
                    else {
                        state.add(IteratorState.TAKEN_FROM_THESE);
                        state.add(IteratorState.TAKEN_FROM_THOSE);
                    }
                }
                else if (state.contains(IteratorState.THESE_ARE_EMPTY)) {
                    state.add(IteratorState.TAKEN_FROM_THOSE);
                }
                else if (state.contains(IteratorState.THOSE_ARE_EMPTY)) {
                    state.add(IteratorState.TAKEN_FROM_THESE);
                }

                return get();
            }
        };
    }

    protected CursorIterator andAlso(final CursorIterator those, final JoinFunction function) {

        final CursorIterator these = this;
        return new CursorIterator() {

            private boolean hasNext;
            private byte prevValue, currValue;
            private int prevCursor, currCursor;

            {
                doNext();
            }


            @Override
            public int cursor() {

                return prevCursor;
            }

            private void doNext() {

                hasNext = false;

                prevValue = currValue;
                prevCursor = currCursor;

                if (these.hasNext() && those.hasNext()) {
                    these.next();
                    those.next();

                    while (these.cursor() != those.cursor()) {
                        if (these.hasNext() && these.cursor() < those.cursor()) {
                            these.next();
                        }
                        else if (those.hasNext() && these.cursor() > those.cursor()) {
                            those.next();
                        }
                        else {
                            return;
                        }
                    }

                    hasNext = true;

                    currValue = function.evaluate(these.get(), those.get());
                    currCursor = these.cursor();
                }
            }

            @Override
            public byte get() {

                return prevValue;
            }

            @Override
            public void set(byte value) {

                throw new UnsupportedOperationException("Composed iterators are read-only for now.");
            }

            @Override
            public boolean hasNext() {

                return hasNext;
            }

            @Override
            public Byte next() {

                doNext();
                return get();
            }
        };
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException("Not supported for now.");
    }

    /**
     * Flushes this iterator.
     */
    public void flush() {

        while (hasNext()) {
            next();
        }
    }
}
