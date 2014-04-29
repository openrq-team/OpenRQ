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


import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.collection.ImmutableList;


/**
 */
final class DataUtils {

    static interface SourceBlockSupplier<SB> {

        SB get(int off, int sbn, int K);
    }


    /**
     * @param <SB>
     * @param clazz
     * @param fecParams
     * @param supplier
     * @return an immutable list of source block encoders/decoders
     */
    static <SB> ImmutableList<SB> partitionData(
        Class<SB> clazz,
        FECParameters fecParams,
        SourceBlockSupplier<SB> supplier) {

        return partitionData(clazz, fecParams, 0, supplier);
    }

    /**
     * @param <SB>
     * @param clazz
     * @param fecParams
     * @param startOffset
     * @param supplier
     * @return an immutable list of source block encoders/decoders
     */
    static <SB> ImmutableList<SB> partitionData(
        Class<SB> clazz,
        FECParameters fecParams,
        int startOffset,
        SourceBlockSupplier<SB> supplier) {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        // partitioned source blocks
        final SB[] srcBlocks = ArrayUtils.newArray(clazz, Z);

        /*
         * The object MUST be partitioned into Z = ZL + ZS contiguous source blocks.
         * Each source block contains a region of the data array, except the last source block
         * which may also contain extra padding.
         */

        final int T = fecParams.symbolSize();
        // source block number (index)
        int sbn;
        int off;

        for (sbn = 0, off = startOffset; sbn < ZL; sbn++, off += KL * T) { // first ZL
            srcBlocks[sbn] = supplier.get(off, sbn, KL);
        }

        for (; sbn < Z; sbn++, off += KS * T) { // last ZS
            srcBlocks[sbn] = supplier.get(off, sbn, KS);
        }

        return ImmutableList.of(srcBlocks);
    }
}
