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

package net.fec.openrq.encoder;


import net.fec.openrq.parameters.FECParameters;


/**
 * A RaptorQ encoder for a data object.
 * <p>
 * An encoder receives a data object (source data) and produces encoding packets (as specified in RFC 6330). The source
 * data is divided into a fixed number of source blocks, and each source block can be encoded independently.
 * <p>
 * An encoder provides a {@link SourceBlockEncoder} object per source block, and each takes care of actually encoding
 * the source data into encoding packets. These encoder objects are accessed via the method
 * {@link #encoderForSourceBlock(int)}.
 * <p>
 * The number of source blocks, the length of the source data and other parameters are specified as the
 * <em>FEC parameters</em>. The method {@link #fecParameters()} provides the associated parameters to the encoder.
 * <p>
 */
public interface DataEncoder {

    /**
     * Returns the FEC parameters associated to this encoder.
     * 
     * @return the FEC parameters associated to this encoder
     */
    public FECParameters fecParameters();

    /**
     * Returns the length of the source data in number of bytes. This value is the one returned by
     * {@code this.fecParameters().dataLength()}.
     * 
     * @return the length of the data in number of bytes
     */
    public long dataLength();

    /**
     * Returns the size of a symbol in number of bytes. This value is the one returned by
     * {@code this.fecParameters().symbolSize()}.
     * 
     * @return the size of a symbol in number of bytes
     */
    public int symbolSize();

    /**
     * Returns the number of source blocks into which the source data is partitioned. This value is the one returned by
     * {@code this.fecParameters().numberOfSourceBlocks()}.
     * 
     * @return the number of source blocks
     */
    public int numberOfSourceBlocks();

    /**
     * Returns an encoder object for a specific source block identified by the given source block number.
     * <p>
     * Note that the provided source block number must be non-negative and less than
     * {@linkplain #numberOfSourceBlocks() the number of source blocks}.
     * 
     * @param sbn
     *            A source block number
     * @return an encoder object for a specific source block
     * @exception IllegalArgumentException
     *                If the provided source block number is invalid
     */
    public SourceBlockEncoder encoderForSourceBlock(int sbn);

    /**
     * Returns a new iterable over all source blocks. The resulting iterable can be iterated using a "foreach" loop.
     * 
     * @return a new iterable over all source blocks
     */
    public Iterable<SourceBlockEncoder> sourceBlockIterable();
}
