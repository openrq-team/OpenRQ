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
package net.fec.openrq;


import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.io.BufferOperation;


/**
 * Container of source symbol data.
 * <p>
 * This class provides two types of source symbol data:
 * <ul>
 * <li>Coding data for encoding/decoding purposes (the length of this data is always equal to the general
 * {@link FECParameters#symbolSize() symbol size}).
 * <li>Transport data for transmission purposes (the length of this data may be smaller than the general symbol size).
 */
interface SourceSymbol {

    /**
     * Returns the size in bytes of this source symbol for coding purposes.
     * 
     * @return the size in bytes of this source symbol for coding purposes
     */
    int codeSize();

    /**
     * Copies data from this source symbol to the provided buffer, for coding purposes.
     * <p>
     * Calling this method has the same effect as calling {@link #getCodeData(ByteBuffer, BufferOperation)
     * getCodeData(dst, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param dst
     *            The destination buffer of copied data
     * @exception BufferOverflowException
     *                If the destination buffer does not have at least {@code codeSize()} number of bytes
     *                {@link Buffer#remaining() available} for storage
     */
    void getCodeData(ByteBuffer dst);

    /**
     * Copies data from this source symbol to the provided buffer, for coding purposes.
     * <p>
     * This method always copies {@link #codeSize()} number of bytes.
     * 
     * @param dst
     *            The destination buffer of copied data
     * @param op
     *            The operation to apply to the destination buffer after the copy
     * @exception BufferOverflowException
     *                If the destination buffer does not have at least {@code codeSize()} number of bytes
     *                {@link Buffer#remaining() available} for storage
     */
    void getCodeData(ByteBuffer dst, BufferOperation op);

    /**
     * Copies code data from the provided buffer to this symbol.
     * <p>
     * Calling this method has the same effect as calling {@link #putCodeData(ByteBuffer, BufferOperation)
     * putCodeData(src, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param src
     *            The source buffer of copied data
     * @exception BufferUnderflowException
     *                If the source buffer does not have at least {@code codeSize()} number of bytes
     *                {@link Buffer#remaining() available}
     */
    void putCodeData(ByteBuffer src);

    /**
     * Copies code data from the provided buffer to this symbol.
     * <p>
     * This method always copies {@link #codeSize()} number of bytes.
     * 
     * @param src
     *            The source buffer of copied data
     * @param op
     *            The operation to apply to the source buffer after the copy
     * @exception BufferUnderflowException
     *                If the source buffer does not have at least {@code codeSize()} number of bytes
     *                {@link Buffer#remaining() available}
     */
    void putCodeData(ByteBuffer src, BufferOperation op);

    /**
     * Returns the size in bytes of this source symbol for transport purposes.
     * 
     * @return the size in bytes of this source symbol for transport purposes
     */
    int transportSize();

    /**
     * Returns a read-only buffer containing the data of this source symbol for transport purposes.
     * <p>
     * The size of transport data is {@link #transportSize()} number of bytes.
     * 
     * @return a read-only buffer containing the data of this source symbol for transport purposes
     */
    ByteBuffer transportData();

    /**
     * Copies symbol data from the provided transport buffer to this symbol.
     * <p>
     * Calling this method has the same effect as calling {@link #putTransportData(ByteBuffer, BufferOperation)
     * putTransportData(src, BufferOperation.ADVANCE_POSITION)}.
     * 
     * @param src
     *            The source buffer of copied data
     * @exception BufferUnderflowException
     *                If the source buffer does not have at least {@code transportSize()} number of bytes
     *                {@link Buffer#remaining() available}
     */
    void putTransportData(ByteBuffer src);

    /**
     * Copies symbol data from the provided transport buffer to this symbol.
     * 
     * @param src
     *            The source buffer of copied data
     * @param op
     *            The operation to apply to the source buffer after the copy
     * @exception BufferUnderflowException
     *                If the source buffer does not have at least {@code transportSize()} number of bytes
     *                {@link Buffer#remaining() available}
     */
    void putTransportData(ByteBuffer src, BufferOperation op);
}
