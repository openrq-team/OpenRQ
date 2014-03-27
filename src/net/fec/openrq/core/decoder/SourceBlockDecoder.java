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

package net.fec.openrq.core.decoder;


import java.nio.ByteBuffer;
import java.util.Set;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface SourceBlockDecoder {

    /**
     * Returns the source block number for the source block currently being decoded.
     * 
     * @return the source block number for the source block currently being decoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the total number of expected source symbols from the source block currently being decoded.
     * 
     * @return the total number of expected source symbols from the source block currently being decoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded contains the source symbol with
     * the provided encoding symbol identifier.
     * <p>
     * Note that a valid encoding symbol identifier for a source symbol must be between {@code 0} (inclusive) and
     * {@code K} (exclusive), where {@code K} is the total number of expected source symbols for the source block
     * currently being decoded.
     * 
     * @param esi
     *            An encoding symbol identifier for a specific source symbol
     * @return {@code true} if, and only if, the source block currently being decoded contains the specified source
     *         symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier does not represent a valid source symbol
     * @see #numberOfSourceSymbols()
     */
    public boolean containsSourceSymbol(int esi);

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded contains the repair symbol with
     * the provided encoding symbol identifier.
     * <p>
     * Note that the encoding symbol identifier must be greater than or equal to {@code K}, where {@code K} is the
     * number of source symbols from the source block currently being decoded.
     * 
     * @param esi
     *            An encoding symbol identifier for a specific repair symbol
     * @return {@code true} if, and only if, the source block currently being decoded contains the specified repair
     *         symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier does not represent a valid repair symbol
     * @see #numberOfSourceSymbols()
     */
    public boolean containsRepairSymbol(int esi);

    /**
     * Returns {@code true} if, and only if, the source block currently being decoded is fully decoded. A source block
     * is considered fully decoded when it contains all of its source symbols.
     * 
     * @return {@code true} if, and only if, the source block currently being decoded is fully decoded
     * @see #containsSourceSymbol(int)
     */
    public boolean isSourceBlockDecoded();

    /**
     * Returns a set of integers containing the encoding symbol identifiers of the missing source symbols from the
     * source block currently being decoded. The returned set has an iteration ordering of ascending encoding symbol
     * identifiers.
     * 
     * @return a set of encoding symbol identifiers of missing source symbols
     */
    public Set<Integer> missingSourceSymbols();

    /**
     * @param esi
     * @param symbolData
     * @return
     */
    public SourceBlockState putSourceSymbol(int esi, ByteBuffer symbolData);

    /**
     * @param esi
     * @param symbolData
     * @param off
     * @return
     */
    public SourceBlockState putSourceSymbol(int esi, byte[] symbolData, int off);

    /**
     * @param esi
     * @param symbolData
     * @return
     */
    public SourceBlockState putRepairSymbol(int esi, ByteBuffer symbolData);

    /**
     * @param esi
     * @param symbolData
     * @param off
     * @return
     */
    public SourceBlockState putRepairSymbol(int esi, byte[] symbolData, int off);
}
