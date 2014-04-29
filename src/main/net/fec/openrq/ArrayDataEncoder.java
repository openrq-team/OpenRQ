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
package net.fec.openrq;


import net.fec.openrq.DataUtils.SourceBlockSupplier;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.collection.ImmutableList;


/**
 * A RaptorQ encoder for an array data object.
 */
public final class ArrayDataEncoder implements DataEncoder {

    static ArrayDataEncoder newEncoder(byte[] array, int offset, FECParameters fecParams) {

        if (offset < 0 || (array.length - offset) < fecParams.dataLength()) {
            throw new IndexOutOfBoundsException();
        }

        return new ArrayDataEncoder(array, offset, fecParams);
    }


    private final byte[] array; // to return to the user
    private final int offset;   // to return to the user

    private final FECParameters fecParams;
    private final ImmutableList<ArraySourceBlockEncoder> srcBlockEncoders;


    private ArrayDataEncoder(byte[] array, int offset, FECParameters fecParams) {

        this.array = array;
        this.offset = offset;

        this.fecParams = fecParams;

        this.srcBlockEncoders = DataUtils.partitionData(
            ArraySourceBlockEncoder.class,
            fecParams,
            offset,
            new SourceBlockSupplier<ArraySourceBlockEncoder>() {

                @Override
                public ArraySourceBlockEncoder get(
                    int off,
                    int sbn,
                    int K)
                {

                    return ArraySourceBlockEncoder.newEncoder(
                        ArrayDataEncoder.this, ArrayDataEncoder.this.array, off,
                        ArrayDataEncoder.this.fecParams,
                        sbn, K);
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
    public ArraySourceBlockEncoder sourceBlock(int sbn) {

        if (sbn < 0 || sbn >= srcBlockEncoders.size()) {
            throw new IllegalArgumentException("invalid source block number");
        }

        return srcBlockEncoders.get(sbn); // list is random access
    }

    @Override
    public Iterable<? extends SourceBlockEncoder> sourceBlockIterable() {

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
