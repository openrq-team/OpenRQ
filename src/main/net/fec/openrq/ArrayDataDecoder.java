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


import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import net.fec.openrq.DataUtils.SourceBlockSupplier;
import net.fec.openrq.decoder.DataDecoder;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.collection.ImmutableList;


/**
 * A RaptorQ decoder for an array data object.
 */
public final class ArrayDataDecoder implements DataDecoder {

    /**
     * @param fecParams
     *            FEC parameters that configure the returned data decoder object
     * @param symbOver
     *            Repair symbol overhead (must be non-negative)
     * @return a data decoder object that decodes source data into an array of bytes
     * @exception NullPointerException
     *                If {@code fecParams} is {@code null}
     * @exception IllegalArgumentException
     *                If {@code fecParams.dataLength() > Integer.MAX_VALUE || extraSymbols < 0}
     */
    static ArrayDataDecoder newDecoder(FECParameters fecParams, int symbOver) {

        // throws NullPointerException if null fecParams
        if (fecParams.dataLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("data length must be at most 2^^31 - 1");
        }
        if (symbOver < 0) {
            throw new IllegalArgumentException("negative symbol overhead");
        }

        final byte[] dataArray = new byte[fecParams.dataLengthAsInt()];
        return new ArrayDataDecoder(dataArray, fecParams, symbOver);
    }


    private final byte[] dataArray;
    private final FECParameters fecParams;
    private final ImmutableList<SourceBlockDecoder> srcBlockDecoders;


    private ArrayDataDecoder(byte[] dataArray, FECParameters fecParams, final int symbOver) {

        this.dataArray = dataArray;
        this.fecParams = fecParams;
        this.srcBlockDecoders = DataUtils.partitionSourceData(
            fecParams,
            SourceBlockDecoder.class, new SourceBlockSupplier<SourceBlockDecoder>() {

                @Override
                public SourceBlockDecoder get(int off, int sbn) {

                    return ArraySourceBlockDecoder.newDecoder(
                        ArrayDataDecoder.this, ArrayDataDecoder.this.dataArray, off,
                        ArrayDataDecoder.this.fecParams,
                        sbn, symbOver);
                }
            });
    }

    @Override
    public FECParameters fecParameters() {

        return fecParams;
    }

    @Override
    public long dataLength() {

        return fecParams.dataLength();
    }

    @Override
    public int symbolSize() {

        return fecParams.symbolSize();
    }

    @Override
    public int numberOfSourceBlocks() {

        return fecParams.numberOfSourceBlocks();
    }

    @Override
    public boolean isDataDecoded() {

        for (SourceBlockDecoder dec : srcBlockDecoders) {
            if (!dec.isSourceBlockDecoded()) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @exception IllegalArgumentException
     *                If the provided source block number is invalid
     */
    @Override
    public SourceBlockDecoder sourceBlock(int sbn) {

        try {
            return srcBlockDecoders.get(sbn); // list is random access
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid source block number");
        }
    }

    @Override
    public Iterable<SourceBlockDecoder> sourceBlockIterable() {

        return srcBlockDecoders;
    }

    /**
     * Returns an array of bytes containing the source data. Use method {@link #isDataDecoded()} to check if the data is
     * complete.
     * 
     * @return an array of bytes containing the source data
     * @see #isDataDecoded()
     */
    public byte[] dataArray() {

        return dataArray;
    }

    /**
     * {@inheritDoc}
     * 
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, byte[] symbols, boolean copySymbols) {

        return DataUtils.parsePacket(this, sbn, esi, symbols, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset and length do not hold
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, byte[] symbols, int off, int len, boolean copySymbols) {

        return DataUtils.parsePacket(this, sbn, esi, symbols, off, len, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(int sbn, int esi, ByteBuffer symbols, boolean copySymbols) {

        return DataUtils.parsePacket(this, sbn, esi, symbols, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception NullPointerException
     *                If {@code ser} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(SerializablePacket ser, boolean copySymbols) {

        return DataUtils.parsePacket(this, ser, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception NullPointerException
     *                If {@code array} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(byte[] array, boolean copySymbols) {

        return DataUtils.parsePacket(this, array, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception IndexOutOfBoundsException
     *                If the pre-conditions on the array offset and length do not hold
     * @exception NullPointerException
     *                If {@code array} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(byte[] array, int off, int len, boolean copySymbols) {

        return DataUtils.parsePacket(this, array, off, len, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @exception NullPointerException
     *                If {@code buffer} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> parsePacket(ByteBuffer buffer, boolean copySymbols) {

        return DataUtils.parsePacket(this, buffer, copySymbols);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code DataInput} object
     * @exception NullPointerException
     *                If {@code in} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> readPacketFrom(DataInput in) throws IOException {

        return DataUtils.readPacketFrom(this, in);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *             If an I/O error occurs while reading from the {@code ReadableByteChannel} object
     * @exception NullPointerException
     *                If {@code ch} is {@code null}
     */
    @Override
    public Parsed<EncodingPacket> readPacketFrom(ReadableByteChannel ch) throws IOException {

        return DataUtils.readPacketFrom(this, ch);
    }
}

