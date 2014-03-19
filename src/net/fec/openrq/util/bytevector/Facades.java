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
package net.fec.openrq.util.bytevector;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class Facades {

    /**
     * Returns a {@code ByteArrayFacade} that wraps an array.
     * 
     * @param array
     *            An array of bytes
     * @return a {@code ByteArrayFacade} that wraps an array
     */
    public static ByteArrayFacade wrapByteArray(final byte[] array) {

        return new ByteArrayFacade() {

            @Override
            public int length() {

                return array.length;
            }

            @Override
            public byte get(int index) {

                return array[index];
            }

            @Override
            public void set(int index, byte value) {

                array[index] = value;
            }

            @Override
            public boolean hasArray() {

                return true;
            }

            @Override
            public byte[] array() {

                return array;
            }
        };
    }

    private Facades() {

        // not instantiable
    }
}
