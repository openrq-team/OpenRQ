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

package net.fec.openrq.core;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.parameters.ParameterIO;
import net.fec.openrq.core.util.arithmetic.ExtraMath;


/**
 * This class represents FEC parameters and provides methods to read from and write to byte arrays and
 * {@link ByteBuffer} objects.
 * <p>
 * The format of the sequence of bytes containing the FEC parameters is defined in RFC 6330 as the <i>Encoded FEC Object
 * Transmission Information (OTI)</i>, which contains the <i>Encoded Common FEC OTI</i> and the <i>Encoded
 * Scheme-specific FEC OTI</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class FECParameters {

    /**
     * Reads from the provided buffer a {@code FECParameters} instance.
     * <p>
     * The provided buffer must have at least 12 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 12 bytes.
     * <p>
     * The returned {@code FECParameters} instance is only {@linkplain #isValid() valid} if the FEC parameters contained
     * inside the buffer have valid values.
     * 
     * @param buffer
     *            A buffer from which a {@code FECParameters} instance is read
     * @return a {@code FECParameters} instance
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public static FECParameters readFromBuffer(ByteBuffer buffer) {

        final long commonFecOTI = ParameterIO.readCommonFecOTI(buffer);        // 8 bytes
        final int schemeSpecFecOTI = ParameterIO.readSchemeSpecFecOTI(buffer); // 4 bytes

        final long F = ParameterIO.extractDataLength(commonFecOTI);
        final int T = ParameterIO.extractSymbolSize(commonFecOTI);
        final int Z = ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
        final int N = ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
        final int Al = ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);

        if (ParameterChecker.areValidFECParameters(F, T, Z, N) && ParameterChecker.isValidSymbolAlignment(Al)) {
            return makeValidFECParameters(commonFecOTI, schemeSpecFecOTI);
        }
        else {
            return makeInvalidFECParameters();
        }
    }

    /**
     * Reads from the provided array starting in a specific index a {@code FECParameters} instance.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * <p>
     * The returned {@code FECParameters} instance is only {@linkplain #isValid() valid} if the FEC parameters contained
     * inside the buffer have valid values.
     * 
     * @param array
     *            An array from which a {@code FECParameters} instance is read
     * @param offset
     *            The starting array index at which a {@code FECParameters} instance is read
     * @return a {@code FECParameters} instance
     * @exception NullPointerException
     *                If the provided array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECParameters} instance cannot be read at the given index
     */
    public static FECParameters readFromArray(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        return readFromBuffer(ByteBuffer.wrap(array, offset, 12));
    }

    static FECParameters makeFECParameters(long F, int T, int Z, int N) {

        if (ParameterChecker.areValidFECParameters(F, T, Z, N)) {
            final long commonFecOTI = ParameterIO.buildCommonFecOTI(F, T);
            final int schemeSpecFecOTI = ParameterIO.buildSchemeSpecFecOTI(Z, N);
            return makeValidFECParameters(commonFecOTI, schemeSpecFecOTI);
        }
        else {
            throw new IllegalArgumentException("invalid FEC parameters");
        }
    }

    private static FECParameters makeValidFECParameters(long commonFecOTI, int schemeSpecFecOTI) {

        return new FECParameters(commonFecOTI, schemeSpecFecOTI, true);
    }

    private static FECParameters makeInvalidFECParameters() {

        return new FECParameters(0L, 0, false);
    }


    private final long commonFecOTI;
    private final int schemeSpecFecOTI;
    private final boolean isValid;


    private FECParameters(long commonFecOTI, int schemeSpecFecOTI, boolean isValid) {

        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
        this.isValid = isValid;
    }

    /**
     * Writes in the provided buffer a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 12 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 12 bytes.
     * 
     * @param buffer
     *            A buffer on which the FEC parameters are written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     * @exception NullPointerException
     *                If the provided the buffer is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 12 bytes remaining
     */
    public void writeToBuffer(ByteBuffer buffer) {

        checkValid();
        ParameterIO.writeCommonFecOTI(commonFecOTI, buffer);         // 8 bytes
        ParameterIO.writeSchemeSpecFecOTI(schemeSpecFecOTI, buffer); // 4 bytes
    }

    /**
     * Writes in the provided array starting in a specific index a sequence of bytes that represent the FEC parameters.
     * <p>
     * The provided array must have at least 12 bytes between the given index and its length.
     * 
     * @param array
     *            An array on which the FEC parameters are written
     * @param offset
     *            The starting array index at which the FEC parameters are written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     * @exception NullPointerException
     *                If the provided the array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the FEC parameters cannot be written at the given index
     */
    public void writeToArray(byte[] array, int offset) {

        checkValid();
        if (offset < 0 || array.length - offset < 12) throw new IndexOutOfBoundsException();
        writeToBuffer(ByteBuffer.wrap(array, offset, 12));
    }

    /**
     * Returns the length of the encodable data in number of bytes.
     * 
     * @return the length of the encodable data in number of bytes
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public long dataLength() {

        checkValid();
        return ParameterIO.extractDataLength(commonFecOTI);
    }

    /**
     * Returns the size of a symbol in number of bytes.
     * 
     * @return the size of a symbol in number of bytes
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int symbolSize() {

        checkValid();
        return ParameterIO.extractSymbolSize(commonFecOTI);
    }

    /**
     * Returns the number of blocks into which the encodable data is partitioned.
     * 
     * @return the number of blocks into which the encodable data is partitioned
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int numberOfSourceBlocks() {

        checkValid();
        return ParameterIO.extractNumSourceBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the number of sub-blocks per source block into which the encodable data is partitioned.
     * 
     * @return the number of sub-blocks per source block into which the encodable data is partitioned
     * @exception IllegalStateException
     *                If this is an invalid {@code FECParamaters} instance
     */
    public int numberOfSubBlocks() {

        checkValid();
        return ParameterIO.extractNumSubBlocks(schemeSpecFecOTI);
    }

    /**
     * Returns the symbol alignment value.
     * 
     * @return the symbol alignment value
     */
    public int symbolAlignment() {

        checkValid();
        return ParameterIO.extractSymbolAlignment(schemeSpecFecOTI);
    }

    /**
     * Returns the total number of source symbols in which the encodable data is divided.
     * 
     * @return the total number of source symbols in which the encodable data is divided
     */
    public int totalSymbols() {

        checkValid();
        // safe cast because F and T are valid, which prevents integer overflow
        return (int)ExtraMath.ceilDiv(dataLength(), symbolSize());
    }

    /**
     * Returns {@code true} if, and only if, this {@code FECParameters} instance is valid, that is, if all FEC
     * parameters have valid values.
     * 
     * @return {@code true} if, and only if, this {@code FECParameters} instance is valid
     */
    public boolean isValid() {

        return isValid;
    }

    private void checkValid() {

        if (!isValid()) throw new IllegalStateException("invalid FEC parameters");
    }
}
