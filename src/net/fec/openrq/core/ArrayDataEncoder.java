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


import net.fec.openrq.core.encoder.DataEncoder;
import net.fec.openrq.core.encoder.SourceBlockEncoder;
import RQLibrary.Partition;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public final class ArrayDataEncoder implements DataEncoder {

    static ArrayDataEncoder newEncoder(byte[] array, int offset, FECParameters fecParams) {

        if (!fecParams.isValid()) {
            throw new IllegalArgumentException("invalid FEC parameters");
        }
        if (offset < 0 || (array.length - offset) < fecParams.dataLength()) {
            throw new IndexOutOfBoundsException();
        }

        return new ArrayDataEncoder(array, offset, fecParams);
    }


    private final byte[] array;
    private final int offset;
    private final FECParameters fecParams;
    private final SourceBlockEncoder[] srcBlockEncoders;


    private ArrayDataEncoder(byte[] array, int offset, FECParameters fecParams) {

        this.array = array;
        this.offset = offset;

        this.fecParams = fecParams;

        this.srcBlockEncoders = partitionData(array, offset, fecParams);
    }

    private static SourceBlockEncoder[] partitionData(
        byte[] array,
        int offset,
        FECParameters fecParams)
    {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        // partitioned source blocks
        final SourceBlockEncoder[] srcBlockEncoders = new ArraySourceBlockEncoder[Z];

        /*
         * The object MUST be partitioned into Z = ZL + ZS contiguous source blocks.
         * Each source block contains a region of the data array, except the last source block
         * which may also contain extra padding.
         */

        final int T = fecParams.symbolSize();
        // source block number (index)
        int sbn;
        int off;

        for (sbn = 0, off = offset; sbn < ZL; sbn++, off += KL * T) { // first ZL
            srcBlockEncoders[sbn] = ArraySourceBlockEncoder.newEncoder(array, off, fecParams, sbn, KL);
        }

        for (; sbn < Z; sbn++, off += KS * T) {// last ZS
            srcBlockEncoders[sbn] = ArraySourceBlockEncoder.newEncoder(array, off, fecParams, sbn, KS);
        }

        return srcBlockEncoders;
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
    public SourceBlockEncoder encoderForSourceBlock(int sbn) {

        if (sbn < 0 || sbn >= srcBlockEncoders.length) {
            throw new IllegalArgumentException("invalid source block number");
        }

        return srcBlockEncoders[sbn];
    }

    /**
     * Returns an array of bytes containing the encodable data.
     * 
     * @return an array of bytes containing the encodable data
     */
    public byte[] dataArray() {

        return array;
    }

    /**
     * Returns the index in the data array of the first encodable byte.
     * 
     * @return the index in the data array of the first encodable byte
     */
    public int dataOffset() {

        return offset;
    }
}
