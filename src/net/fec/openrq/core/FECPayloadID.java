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

import net.fec.openrq.core.parameters.FECParameters;
import net.fec.openrq.core.parameters.ParameterChecker;
import net.fec.openrq.core.parameters.ParameterIO;
import net.fec.openrq.core.util.Optional;


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
     * Reads from the provided buffer an optional {@code FECPayloadID} instance given FEC parameters associated to some
     * data.
     * <p>
     * The provided buffer must have at least 4 bytes {@linkplain ByteBuffer#remaining() remaining}. If this method
     * returns normally, the position of the provided buffer will have advanced by 4 bytes.
     * <p>
     * A {@code FECPayloadID} instance is only {@linkplain Optional#isPresent() present} inside the returned optional
     * instance if the payload ID parameters contained inside the buffer have valid values according to the provided FEC
     * parameters.
     * 
     * @param buffer
     *            A buffer from which a {@code FECPayloadID} instance is read
     * @param fecParams
     *            FEC parameters associated to some data object
     * @return an optional {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If any argument is {@code null}
     * @exception BufferUnderflowException
     *                If the provided buffer has less than 4 bytes remaining
     */
    public static Optional<FECPayloadID> readFromBuffer(ByteBuffer buffer, FECParameters fecParams) {

        final int fecPayloadID = ParameterIO.readFECpayloadID(buffer); // 4 bytes

        final int sbn = ParameterIO.extractSourceBlockNumber(fecPayloadID);
        final int esi = ParameterIO.extractEncodingSymbolID(fecPayloadID);

        if (ParameterChecker.isValidFECPayloadID(sbn, esi, fecParams.numberOfSourceBlocks())) {
            return Optional.of(newInstance(sbn, esi));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Reads from the provided array starting in a specific index an optional {@code FECPayloadID} instance given FEC
     * parameters associated to some data.
     * <p>
     * The provided array must have at least 4 bytes between the given index and its length.
     * <p>
     * A {@code FECPayloadID} instance is only {@linkplain Optional#isPresent() present} inside the returned optional
     * instance if the payload ID parameters contained inside the array have valid values according to the provided FEC
     * parameters.
     * 
     * @param array
     *            An array from which a {@code FECPayloadID} instance is read
     * @param offset
     *            The starting array index at which a {@code FECPayloadID} instance is read
     * @param fecParams
     *            FEC parameters associated to some data object
     * @return an optional {@code FECPayloadID} instance
     * @exception NullPointerException
     *                If any argument is {@code null}
     * @exception IndexOutOfBoundsException
     *                If a {@code FECPayloadID} instance cannot be read at the given index
     */
    public static Optional<FECPayloadID> readFromArray(byte[] array, int offset, FECParameters fecParams) {

        if (offset < 0 || array.length - offset < 4) throw new IndexOutOfBoundsException();
        return readFromBuffer(ByteBuffer.wrap(array, offset, 4), fecParams);
    }

    /**
     * Returns a new instance, given specific FEC payload ID parameters.
     * 
     * @param sbn
     *            The source block number associated to some source block
     * @param esi
     *            The encoding symbol identifier associated to the first symbol in an encoding packet
     * @param fecParams
     *            FEC parameters associated to some data
     * @return a new {@code FECPayloadID} instance
     */
    public static FECPayloadID newPayloadID(int sbn, int esi, FECParameters fecParams) {

        if (ParameterChecker.isValidFECPayloadID(sbn, esi, fecParams.numberOfSourceBlocks())) {
            return newInstance(sbn, esi);
        }
        else {
            throw new IllegalArgumentException("invalid FEC Payload ID");
        }

    }

    private static FECPayloadID newInstance(int sbn, int esi) {

        return new FECPayloadID(ParameterIO.buildFECpayloadID(sbn, esi));
    }


    private final int fecPayloadID;


    private FECPayloadID(int fecPayloadID) {

        this.fecPayloadID = fecPayloadID;
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
     * @exception NullPointerException
     *                If the provided the buffer is {@code null}
     * @exception ReadOnlyBufferException
     *                If the provided buffer is read-only
     * @exception BufferOverflowException
     *                If the provided buffer has less than 4 bytes remaining
     */
    public void writeToBuffer(ByteBuffer buffer) {

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
     * @exception NullPointerException
     *                If the provided the array is {@code null}
     * @exception IndexOutOfBoundsException
     *                If the FEC payload ID cannot be written at the given index
     */
    public void writeToArray(byte[] array, int offset) {

        if (offset < 0 || array.length - offset < 4) throw new IndexOutOfBoundsException();
        writeToBuffer(ByteBuffer.wrap(array, offset, 4));
    }

    /**
     * Returns the source block number associated to some source block.
     * 
     * @return the source block number associated to some source block
     */
    public int sourceBlockNumber() {

        return ParameterIO.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * Returns the encoding symbol identifier associated to the first symbol in an encoding packet.
     * 
     * @return the encoding symbol identifier associated to the first symbol in an encoding packet
     */
    public int encodingSymbolID() {

        return ParameterIO.extractEncodingSymbolID(fecPayloadID);
    }
}
