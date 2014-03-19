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


/**
 * This class represents a payload identifier for an encoding symbol and provides methods to read from and write to byte
 * arrays and {@link ByteBuffer} objects.
 * <p>
 * The format of the sequence of bytes containing the payload identifier is defined in RFC 6330 as the <i>FEC Payload
 * ID</i>.
 * 
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class FECPayloadID {

    /**
     * Reads from the provided buffer a {@code FECPayloadID} instance given a valid {@code FECParameters} instance.
     * <p>
     * The provided buffer must have at least 4 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 4 bytes.
     * <p>
     * The returned {@code FECPayloadID} instance is only {@linkplain #isValid() valid} if the payload identifier
     * parameters contained inside the buffer have valid values according to the provided {@code FECparameters}
     * instance.
     * 
     * @param buffer
     *            A buffer from which a {@code FECPayloadID} instance is read
     * @param fecParams
     *            FEC parameters associated to some data object
     * @return a {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If any argument is {@code null}
     * @exception IllegalArgumentException
     *                If the provided {@code FECParameters} instance is {@linkplain FECParameters#isValid() invalid}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 4 bytes remaining
     */
    public static FECPayloadID readFromBuffer(ByteBuffer buffer, FECParameters fecParams) {

        if (!fecParams.isValid()) throw new IllegalArgumentException("invalid FEC parameters");

        final int fecPayloadID = ParameterIO.readFECpayloadID(buffer); // 4 bytes

        final int sbn = ParameterIO.extractSourceBlockNumber(fecPayloadID);
        final int esi = ParameterIO.extractEncodingSymbolID(fecPayloadID);

        if (ParameterChecker.isValidFECPayloadID(sbn, esi, fecParams.numberOfSourceBlocks())) {
            return makeValidPayloadID(fecPayloadID);
        }
        else {
            return makeInvalidPayloadID();
        }
    }

    /**
     * Reads from the provided array starting in a specific index a {@code FECPayloadID} instance given a valid
     * {@code FECParameters} instance.
     * <p>
     * The provided array must have at least 4 bytes between the given index and its length.
     * <p>
     * The returned {@code FECPayloadID} instance is only {@linkplain #isValid() valid} if the payload identifier
     * parameters contained inside the array have valid values according to the provided {@code FECparameters} instance.
     * 
     * @param array
     *            An array from which a {@code FECPayloadID} instance is read
     * @param offset
     *            The starting array index at which a {@code FECPayloadID} instance is read
     * @param fecParams
     *            FEC parameters associated to some data object
     * @return a {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If any argument is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECPayloadID} instance cannot be read at the given index
     * @exception IllegalArgumentException
     *                If the provided {@code FECParameters} instance is {@linkplain FECParameters#isValid() invalid}
     */
    public static FECPayloadID readFromArray(byte[] array, int offset, FECParameters fecParams) {

        if (offset < 0 || array.length - offset < 4) throw new IndexOutOfBoundsException();
        return readFromBuffer(ByteBuffer.wrap(array, offset, 4), fecParams);
    }

    // requires valid FECParameters instance
    static FECPayloadID makeFECPayloadID(int sbn, int esi, FECParameters fecParams) {

        if (ParameterChecker.isValidFECPayloadID(sbn, esi, fecParams.numberOfSourceBlocks())) {
            return makeValidPayloadID(ParameterIO.buildFECpayloadID(sbn, esi));
        }
        else {
            throw new IllegalArgumentException("invalid FEC Payload ID");
        }

    }

    private static FECPayloadID makeValidPayloadID(int fecPayloadID) {

        return new FECPayloadID(fecPayloadID, true);
    }

    private static FECPayloadID makeInvalidPayloadID() {

        return new FECPayloadID(0, false);
    }


    private final int fecPayloadID;
    private final boolean isValid;


    private FECPayloadID(int fecPayloadID, boolean isValid) {

        this.fecPayloadID = fecPayloadID;
        this.isValid = isValid;
    }

    /**
     * Writes in the provided buffer a sequence of bytes that represent the FEC payload ID.
     * <p>
     * The provided buffer must not be {@linkplain ByteBuffer#isReadOnly() read-only}, and must have at least 4 bytes
     * {@linkplain ByteBuffer#remaining() remaining}. If this method returns normally, the position of the provided
     * buffer will have advanced by 4 bytes.
     * 
     * @param buffer
     *            A buffer on which the FEC payload ID is written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If the provided the buffer is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 4 bytes remaining
     */
    public void writeToBuffer(ByteBuffer buffer) {

        checkValid();
        ParameterIO.writeFECpayloadID(fecPayloadID, buffer); // 4 bytes
    }

    /**
     * Writes in the provided array starting in a specific index a sequence of bytes that represent the FEC payload ID.
     * <p>
     * The provided array must have at least 4 bytes between the given index and its length.
     * 
     * @param array
     *            An array on which the FEC payload ID is written
     * @param offset
     *            The starting array index at which the FEC payload ID is written
     * @exception IllegalStateException
     *                If this is an invalid {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If the provided the array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the FEC payload ID cannot be written at the given index
     */
    public void writeToArray(byte[] array, int offset) {

        checkValid();
        if (offset < 0 || array.length - offset < 4) throw new IndexOutOfBoundsException();
        writeToBuffer(ByteBuffer.wrap(array, offset, 4));
    }

    /**
     * Returns the source block number associated to some source block.
     * 
     * @return the source block number associated to some source block
     * @exception IllegalStateException
     *                If this is an invalid {@code FECPayloadID} instance
     */
    public int sourceBlockNumber() {

        checkValid();
        return ParameterIO.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * Returns the encoding symbol identifier associated to the first symbol in an encoding packet.
     * 
     * @return the encoding symbol identifier associated to the first symbol in an encoding packet
     * @exception IllegalStateException
     *                If this is an invalid {@code FECPayloadID} instance
     */
    public int encodingSymbolID() {

        checkValid();
        return ParameterIO.extractEncodingSymbolID(fecPayloadID);
    }

    /**
     * Returns {@code true} if, and only if, this {@code FECPayloadID} instance is valid, that is, if all payload
     * identifier parameters have valid values.
     * 
     * @return {@code true} if, and only if, this {@code FECPayloadID} instance is valid
     */
    public boolean isValid() {

        return isValid;
    }

    private void checkValid() {

        if (!isValid()) throw new IllegalStateException("invalid FEC Payload ID");
    }
}
