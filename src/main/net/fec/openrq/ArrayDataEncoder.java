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


import java.util.Objects;

import net.fec.openrq.DataUtils.SourceBlockSupplier;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.checking.Indexables;
import net.fec.openrq.util.collection.ImmutableList;


/**
 * A RaptorQ encoder for an array data object.
 */
public final class ArrayDataEncoder implements DataEncoder {

    /**
     * @param fecParams
     *            FEC parameters that configure the returned data encoder object
     * @param data
     *            An array of bytes containing the source data to be encoded
     * @param offset
     *            The index in the array where the source data begins
     * @return a data encoder object backed by an array of bytes
     * @exception NullPointerException
     *                If {@code data} or {@code fecParams} are {@code null}
     * @exception IllegalArgumentException
     *                If {@code fecParams.dataLength() > Integer.MAX_VALUE}
     * @exception IndexOutOfBoundsException
     *                If {@code offset < 0 || fecParams.dataLength() > (data.length - offset)}
     */
    static ArrayDataEncoder newEncoder(byte[] data, int offset,
        FECParameters fecParams) {

        Objects.requireNonNull(data);
        // throws NullPointerException if null fecParams
        if (fecParams.dataLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "data length must be at most 2^^31 - 1");
        }
        Indexables.checkOffsetLengthBounds(offset, fecParams.dataLengthAsInt(),
            data.length);

        return new ArrayDataEncoder(data, offset, fecParams);
    }


    private final byte[] array; // to return to the user
    private final int offset; // to return to the user

    private final FECParameters fecParams;
    private final ImmutableList<SourceBlockEncoder> srcBlockEncoders;


    private ArrayDataEncoder(byte[] array, int offset, FECParameters fecParams) {

        this.array = array;
        this.offset = offset;

        this.fecParams = fecParams;

        this.srcBlockEncoders = DataUtils.partitionSourceData(
            fecParams, offset,
            SourceBlockEncoder.class, new SourceBlockSupplier<SourceBlockEncoder>() {

                @Override
                public SourceBlockEncoder get(int off, int sbn) {

                    return ArraySourceBlockEncoder.newEncoder(
                        ArrayDataEncoder.this,
                        ArrayDataEncoder.this.array, off,
                        ArrayDataEncoder.this.fecParams, sbn);
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

    /**
     * {@inheritDoc}
     * 
     * @exception IllegalArgumentException
     *                If the provided source block number is invalid
     * @see #numberOfSourceBlocks()
     */
    @Override
    public SourceBlockEncoder sourceBlock(int sbn) {

        try {
            return srcBlockEncoders.get(sbn); // list is random access
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid source block number");
        }
    }

    @Override
    public Iterable<SourceBlockEncoder> sourceBlockIterable() {

        return srcBlockEncoders;
    }

    /**
     * Returns an array of bytes containing the source data.
     * 
     * @return an array of bytes containing the source data
     */
    public byte[] dataArray() {

        return array;
    }

    /**
     * Returns the index in the source data array of the first byte.
     * 
     * @return the index in the source data array of the first byte
     */
    public int dataOffset() {

        return offset;
    }
}

