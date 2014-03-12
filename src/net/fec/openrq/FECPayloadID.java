/* 
 * Copyright 2014 Jose Lopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fec.openrq;


import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import net.fec.openrq.parameters.ParameterChecker;
import net.fec.openrq.parameters.ParameterIO;


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

    // Enum state to avoid throwing exceptions when the user parses illegal parameters.
    public static enum Validity {

        VALID,
        INVALID_SOURCE_BLOCK_NUMBER,
        INVALID_ENCODING_SYMBOL_ID;
    }


    /**
     * Reads from the provided buffer a {@code FECPayloadID} instance.
     * <p>
     * The provided buffer must have at least 4 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 4 bytes.
     * <p>
     * The returned {@code FECPayloadID} instance is only {@linkplain #isValid() valid} if the payload identifier
     * parameters contained inside the buffer have valid values. If some parameter value is invalid, the method
     * {@link #getValidity()} can be used to infer which parameter value is invalid.
     * 
     * @param buffer
     *            A buffer from which a {@code FECPayloadID} instance is read
     * @return a {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If the provided buffer is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 4 bytes remaining
     */
    public static FECPayloadID readFromBuffer(ByteBuffer buffer) {

        final int fecPayloadID = ParameterIO.readFECpayloadID(buffer); // 4 bytes

        if (!ParameterChecker.isValidSourceBlockNumber(ParameterIO.extractSourceBlockNumber(fecPayloadID))) {
            return new FECPayloadID(0, FECPayloadID.Validity.INVALID_SOURCE_BLOCK_NUMBER);
        }
        else if (!ParameterChecker.isValidEncodingSymbolID(ParameterIO.extractEncodingSymbolID(fecPayloadID))) {
            return new FECPayloadID(0, FECPayloadID.Validity.INVALID_ENCODING_SYMBOL_ID);
        }
        else {
            return new FECPayloadID(fecPayloadID, FECPayloadID.Validity.VALID);
        }
    }

    /**
     * Reads from the provided array starting in a specific index a {@code FECPayloadID} instance.
     * <p>
     * The provided array must have at least 4 bytes between the given index and its length.
     * <p>
     * The returned {@code FECPayloadID} instance is only {@linkplain #isValid() valid} if the payload identifier
     * parameters contained inside the array have valid values. If some parameter value is invalid, the method
     * {@link #getValidity()} can be used to infer which parameter value is invalid.
     * 
     * @param array
     *            An array from which a {@code FECPayloadID} instance is read
     * @param offset
     *            The starting array index at which a {@code FECPayloadID} instance is read
     * @return a {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If the provided array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECPayloadID} instance cannot be read at the given index
     */
    public static FECPayloadID readFromArray(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 4) throw new IndexOutOfBoundsException();
        return readFromBuffer(ByteBuffer.wrap(array, offset, 4));
    }

    static FECPayloadID makeFECPayloadID(int sbn, int esi) {

        if (!ParameterChecker.isValidSourceBlockNumber(sbn)) throw new IllegalArgumentException("invalid SBN");
        if (!ParameterChecker.isValidEncodingSymbolID(esi)) throw new IllegalArgumentException("invalid ESI");

        final int fecPayloadID = ParameterIO.buildFECpayloadID(sbn, esi);
        return new FECPayloadID(fecPayloadID, FECPayloadID.Validity.VALID);
    }


    private final int fecPayloadID;
    private final FECPayloadID.Validity validity;


    private FECPayloadID(int fecPayloadID, FECPayloadID.Validity validity) {

        this.fecPayloadID = fecPayloadID;
        this.validity = validity;
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

        return validity == FECPayloadID.Validity.VALID;
    }

    /**
     * Returns an enum value indicating the (in)validity of this {@code FECPayloadID} instance.
     * 
     * @return an enum value indicating the (in)validity of this {@code FECPayloadID} instance
     */
    public FECPayloadID.Validity getValidity() {

        return validity;
    }

    private void checkValid() {

        if (!isValid()) {
            final String errorMsg;
            switch (validity) {
                case INVALID_SOURCE_BLOCK_NUMBER:
                    errorMsg = "invalid source block number";
                break;
                case INVALID_ENCODING_SYMBOL_ID:
                    errorMsg = "invalid encoding symbol identifier";
                break;
                default:
                    // should never happen
                    throw new AssertionError("unknown enum type");
            }
            throw new IllegalStateException(errorMsg);
        }
    }
}
