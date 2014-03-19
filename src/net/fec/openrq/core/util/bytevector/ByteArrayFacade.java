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
package net.fec.openrq.core.util.bytevector;

/**
 * Simple interface that mimics an array.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface ByteArrayFacade {

    /**
     * Returns the (fixed) length in number of bytes of this facade.
     * 
     * @return the number of bytes in this facade
     */
    public int length();

    /**
     * Returns the byte at a given position.
     * 
     * @param index
     *            A position in this facade (must be non-negative and less than {@code length()})
     * @return the byte at position {@code index}
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0} or if {@code index >= length()}
     */
    public byte get(int index);

    /**
     * Replaces the byte at a given position with the provided value.
     * 
     * @param index
     *            A position in this facade (must be non-negative and less than {@code length()})
     * @param value
     *            The value to be set at position {@code index}
     * @exception IndexOutOfBoundsException
     *                If {@code index < 0} or if {@code index >= length()}
     */
    public void set(int index, byte value);

    /**
     * Returns {@code true} if this facade has an underlying array.
     * 
     * @return {@code true} if this facade has an underlying array
     */
    public boolean hasArray();

    /**
     * Returns the underlying array of bytes if this facade has one, otherwise an {@code UnsupportedOperationException}
     * is thrown.
     * 
     * @return the underlying array of bytes (if it exists)
     * @exception UnsupportedOperationException
     *                If this facade does not have an underlying array of bytes
     */
    public byte[] array();
}
