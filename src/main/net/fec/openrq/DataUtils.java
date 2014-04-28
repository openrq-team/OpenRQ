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


import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import net.fec.openrq.util.array.ArrayUtils;
import net.fec.openrq.util.collection.ImmutableList;


/**
 */
final class DataUtils {

    static interface SBEFactory<E extends SourceBlockEncoder> {

        E newSBE(int offset, int sbn, int K);
    }

    static interface SBDFactory<D extends SourceBlockDecoder> {

        D newSBD(int offset, int sbn, int K);
    }


    /**
     * @param clazz
     * @param factory
     * @param fecParams
     * @return an immutable list of source block encoders
     */
    static <S extends SourceBlockEncoder> ImmutableList<S> partitionEncData(
        Class<S> clazz,
        FECParameters fecParams,
        SBEFactory<S> factory)
    {

        return partitionEncData(clazz, fecParams, 0, factory);
    }

    /**
     * @param clazz
     * @param fecParams
     * @param startOffset
     * @param factory
     * @return an immutable list of source block encoders
     */
    static <E extends SourceBlockEncoder> ImmutableList<E> partitionEncData(
        Class<E> clazz,
        FECParameters fecParams,
        int startOffset,
        SBEFactory<E> factory)
    {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        // partitioned source blocks
        final E[] srcBlockEncoders = ArrayUtils.newArray(clazz, Z);

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
            srcBlockEncoders[sbn] = factory.newSBE(off, sbn, KL);
        }

        for (; sbn < Z; sbn++, off += KS * T) { // last ZS
            srcBlockEncoders[sbn] = factory.newSBE(off, sbn, KS);
        }

        return ImmutableList.of(srcBlockEncoders);
    }

    /**
     * @param clazz
     * @param fecParams
     * @param factory
     * @return an immutable list of source block decoders
     */
    static <D extends SourceBlockDecoder> ImmutableList<D> partitionDecData(
        Class<D> clazz,
        FECParameters fecParams,
        SBDFactory<D> factory)
    {

        return partitionDecData(clazz, fecParams, 0, factory);
    }

    /**
     * @param clazz
     * @param fecParams
     * @param startOffset
     * @param factory
     * @return an immutable list of source block decoders
     */
    static <D extends SourceBlockDecoder> ImmutableList<D> partitionDecData(
        Class<D> clazz,
        FECParameters fecParams,
        int startOffset,
        SBDFactory<D> factory)
    {

        final int Kt = fecParams.totalSymbols();
        final int Z = fecParams.numberOfSourceBlocks();

        // (KL, KS, ZL, ZS) = Partition[Kt, Z]
        final Partition KZ = new Partition(Kt, Z);
        final int KL = KZ.get(1);
        final int KS = KZ.get(2);
        final int ZL = KZ.get(3);

        // partitioned source blocks
        final D[] srcBlockDecoders = ArrayUtils.newArray(clazz, Z);

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
            srcBlockDecoders[sbn] = factory.newSBD(off, sbn, KL);
        }

        for (; sbn < Z; sbn++, off += KS * T) { // last ZS
            srcBlockDecoders[sbn] = factory.newSBD(off, sbn, KS);
        }

        return ImmutableList.of(srcBlockDecoders);
    }
}
