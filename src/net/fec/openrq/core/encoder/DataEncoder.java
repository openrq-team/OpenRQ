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

package net.fec.openrq.core.encoder;


import net.fec.openrq.core.FECParameters;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface DataEncoder {

    /**
     * Returns the FEC parameters associated to this encoder. The returned {@code FECParameters} instance is always
     * valid.
     * 
     * @return the FEC parameters associated to this encoder
     */
    public FECParameters fecParameters();

    /**
     * Returns the length of the data in number of bytes. This value is the one returned by
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
     * Returns the number of source blocks. This value is the one returned by
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
}
