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

package net.fec.openrq.encoder;


import net.fec.openrq.EncodingPacket;
import net.fec.openrq.parameters.ParameterChecker;


/**
 * An encoder for a source block.
 * <p>
 * A source block encoder is retrieved from a {@link DataEncoder} object, which is associated to some source data.
 * Source data is divided into source blocks and each source block is independently encoded by an instance of this
 * interface. The source block being encoded is divided into source symbols, which together with repair symbols (extra
 * encoded data) form the <em>encoding symbols</em>. The encoding symbols are transmitted inside encoding packets to a
 * RaptorQ decoder (as specified in RFC 6330).
 * <p>
 * A source block can be encoded by an instance of this interface, independently from other source blocks, and the block
 * is identified by a source block number, which is carried inside an encoding packet. The method
 * {@link #sourceBlockNumber()} provides the source block number that identifies the source block being encoded.
 * Additionally, the number of source symbols into which the source block is divided is given by the method
 * {@link #numberOfSourceSymbols()}.
 * <p>
 * The method {@link #encodingPacket(int)} returns an encoding packet with a specific encoding symbol. The methods
 * {@link #sourcePacketsIterable()} and {@link #repairPacketsIterable(int)} return iterable objects that iterate over
 * all source packets (encoding packets with a source symbol each) and over some repair packets (encoding packets with a
 * repair symbol each). More customizable iterable objects can be created using the {@link IterableBuilder} class.
 */
public interface SourceBlockEncoder {

    /**
     * A builder class for an {@link Iterable} over {@link EncodingPacket} instances.
     * <p>
     * This class builds iterables over a sequence of encoding packets (whose encoding symbols have contiguous encoding
     * symbol identifiers (ESI)). The resulting iterable can be configured to {@linkplain #startAt(int) start} or
     * {@linkplain #endAt(int) end} at a particular ESI
     */
    public static interface IterableBuilder {

        /**
         * Defines the encoding symbol identifier (ESI) of the symbol in the first encoding packet. If a starting ESI is
         * not defined, then the resulting iterable starts at ESI 0.
         * <p>
         * <b>Note:</b> <em>If the starting ESI is greater than the {@linkplain #endAt(int) ending} ESI, then the ending
         * ESI will be set to the starting ESI.</em>
         * <p>
         * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided
         * the source block being encoded, and {@code max_esi} as the maximum value for the encoding symbol identifier,
         * then the following must be true, otherwise an {@code IllegalArgumentException} is thrown:
         * <ul>
         * <li>{@code esi} &ge; 0
         * <li>{@code esi} &le; {@code max_esi} </ul>
         * 
         * @param esi
         *            The encoding symbol identifier of the symbol in the first encoding packet
         * @return this builder (for chained invocation)
         * @exception IllegalArgumentException
         *                If the provided encoding symbol identifier is invalid
         */
        public IterableBuilder startAt(int esi);

        /**
         * Convenience method for defining the {@linkplain #startAt(int) starting} encoding symbol identifier (ESI) to
         * the ESI of the first source symbol.
         * 
         * @return this builder (for chained invocation)
         */
        public IterableBuilder startAtInitialSourceSymbol();

        /**
         * Convenience method for defining the {@linkplain #startAt(int) starting} encoding symbol identifier (ESI) to
         * the ESI of the first repair symbol.
         * 
         * @return this builder (for chained invocation)
         */
        public IterableBuilder startAtInitialRepairSymbol();

        /**
         * Defines the encoding symbol identifier (ESI) of the symbol in the last encoding packet. If an ending ESI is
         * not defined, then the resulting iterable ends at the {@linkplain ParameterChecker#maxEncodingSymbolID()
         * maximum value for the ESI}.
         * <p>
         * <b>Note:</b>
         * <em>If the ending ESI is less than the {@linkplain #startAt(int) starting} ESI, then the starting
         * ESI will be set to the ending ESI.</em>
         * <p>
         * <b><em>Bounds checking</em></b> - If we have <b>K</b> as the number of source symbols into which is divided
         * the source block being encoded, and <b>max_esi</b> as the maximum value for the encoding symbol identifier,
         * then the following must be true, otherwise an {@code IllegalArgumentException} is thrown:
         * <ul>
         * <li>{@code esi} &ge; 0
         * <li>{@code esi} &le; {@code max_esi} </ul>
         * 
         * @param esi
         *            The encoding symbol identifier of the symbol in the last encoding packet
         * @return this builder (for chained invocation)
         * @exception IllegalArgumentException
         *                If the provided encoding symbol identifier is invalid
         */
        public IterableBuilder endAt(int esi);

        /**
         * Convenience method for defining the {@linkplain #endAt(int) ending} encoding symbol identifier (ESI) to the
         * ESI of the last source symbol.
         * 
         * @return this builder (for chained invocation)
         */
        public IterableBuilder endAtFinalSourceSymbol();

        /**
         * Returns the resulting iterable over encoding packets based on the currently defined properties. Each iterated
         * encoding packet contains a single encoding symbol within.
         * <p>
         * <b>Note:</b> <em>The iterator from the resulting iterable will <b>not</b> support the
         * {@linkplain java.util.Iterator#remove() remove()} method.</em>
         * 
         * @return the resulting iterable over encoding packets
         */
        public Iterable<EncodingPacket> build();
    }


    /**
     * Returns the data encoder object from which this source block encoder was retrieved.
     * 
     * @return the data encoder object from which this source block encoder was retrieved
     */
    public DataEncoder dataEncoder();

    /**
     * Returns the identifier of the source block being encoded.
     * 
     * @return the identifier of the source block being encoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the total number of source symbols into which is divided the source block being encoded.
     * 
     * @return the total number of source symbols into which is divided the source block being encoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns an encoding packet with an encoding symbol from the source block being encoded. The symbol is a source or
     * a repair symbol according to the provided identifier.
     * <p>
     * More specifically, if we have {@code sbn} as the source block number for the source block being encoded, then
     * this method returns an encoding packet with an encoding symbol identified by <code>&lt;sbn, esi&gt;</code>.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have{@code K} as the number of source symbols into which is divided the
     * source block being encoded, and {@code max_esi} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum
     * value for the encoding symbol identifier}, then the following must be true, otherwise an
     * {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li>{@code esi} &ge; 0
     * <li>{@code esi} &le; {@code max_esi} </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the encoding symbol in the returned packet
     * @return an encoding packet with an encoding symbol from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket encodingPacket(int esi);

    /**
     * Returns an encoding packet with a source symbol from the source block being encoded.
     * <p>
     * More specifically, if we have {@code sbn} as the source block number for the source block being encoded, then
     * this method returns an encoding packet with a source symbol identified by <code>&lt;sbn, esi&gt;</code>.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being encoded, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li>{@code esi} &ge; 0
     * <li>{@code esi} &lt; {@code K} </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the source symbol in the returned packet
     * @return an encoding packet with a source symbol from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket sourcePacket(int esi);

    /**
     * Returns an encoding packet with multiple source symbols from the source block being encoded.
     * <p>
     * More specifically, if we have {@code sbn} as the source block number for the source block being encoded, then
     * this method returns an encoding packet with:
     * <ul>
     * <li>a 1st source symbol identified by <code>&lt;sbn, esi&gt;</code>,
     * <li>a 2nd source symbol identified by <code>&lt;sbn, esi+1&gt;</code>,
     * <li>a 3rd source symbol identified by <code>&lt;sbn, esi+2&gt;</code>,
     * <li>etc.
     * </ul>
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being encoded, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li>{@code esi} &ge; 0
     * <li>{@code esi} &lt; {@code K} <li>{@code numSymbols} &gt; 0
     * <li>{@code numSymbols} &le; ({@code K - esi})
     * </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the first source symbol in the returned packet
     * @param numSymbols
     *            The number of source symbols to be placed in the returned packet
     * @return an encoding packet with multiple source symbols from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket sourcePacket(int esi, int numSymbols);

    /**
     * Returns an encoding packet with a repair symbol from the source block being encoded.
     * <p>
     * More specifically, if we have {@code sbn} as the source block number for the source block being encoded, then
     * this method returns an encoding packet with a repair symbol identified by <code>&lt;sbn, esi&gt;</code>.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being encoded, and {@code max_esi} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum
     * value for the encoding symbol identifier}, then the following must be true, otherwise an
     * {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li>{@code esi} &ge; {@code K} <li>{@code esi} &le; {@code max_esi} </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the repair symbol in the returned packet
     * @return an encoding packet with a repair symbol from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket repairPacket(int esi);

    /**
     * Returns an encoding packet with multiple repair symbols from the source block being encoded.
     * <p>
     * More specifically, if we have {@code sbn} as the source block number for the source block being encoded, then
     * this method returns an encoding packet with:
     * <ul>
     * <li>a 1st repair symbol identified by <code>&lt;sbn, esi&gt;</code>,
     * <li>a 2nd repair symbol identified by <code>&lt;sbn, esi+1&gt;</code>,
     * <li>a 3rd repair symbol identified by <code>&lt;sbn, esi+2&gt;</code>,
     * <li>etc.
     * </ul>
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being encoded, and {@code max_esi} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum
     * value for the encoding symbol identifier}, then the following must be true, otherwise an
     * {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li>{@code esi} &ge; {@code K} <li>{@code esi} &le; {@code max_esi} <li>{@code numSymbols} &gt; 0
     * <li>{@code numSymbols} &le; ({@code 1 + max_esi - esi})
     * </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the first repair symbol in the returned packet
     * @param numSymbols
     *            The number of repair symbols to be placed in the returned packet
     * @return an encoding packet with multiple repair symbols from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket repairPacket(int esi, int numSymbols);

    /**
     * Returns a new builder object for an iterable over encoding packets.
     * <p>
     * For example, the following code shows how to retrieve an iterable over encoding packets that starts at encoding
     * symbol identifier (ESI) 51 and ends at ESI 100:
     * <pre>
     * Iterable&lt;EncodingPacket&gt; iterable = newIterableBuilder().startAt(50)
     * .endAt(100)
     * .build();</pre>
     * The resulting iterable can be iterated using a "foreach" loop:
     * <pre>
     * for (EncodingPacket packet : iterable) {
     * // process packet...
     * }</pre>
     * 
     * @return a new builder object for an iterable over encoding packets
     */
    public IterableBuilder newIterableBuilder();

    /**
     * Returns an iterable over all source packets, each packet containing one source symbol.
     * <p>
     * The resulting iterable can be iterated using a "foreach" loop:
     * <pre>
     * for (EncodingPacket packet : iterable) {
     * // process packet...
     * }</pre>
     * <p>
     * Calling this method is the same as calling:
     * <pre>
     * encoder.newIterableBuilder().startAtInitialSourceSymbol()
     * .endAtFinalSourceSymbol()
     * .build();
     * </pre>
     * 
     * @return an iterable over all source packets
     * @see #newIterableBuilder()
     */
    public Iterable<EncodingPacket> sourcePacketsIterable();

    /**
     * Returns an iterable over a number of repair packets, each packet containing one repair symbol.
     * <p>
     * The resulting iterable can be iterated using a "foreach" loop:
     * <pre>
     * for (EncodingPacket packet : iterable) {
     * // process packet...
     * }</pre>
     * <p>
     * Calling this method is the same as calling:
     * <pre>
     * encoder.newIterableBuilder().startAtInitialRepairSymbol()
     * .endAt(encoder.numberOfSourceSymbols() + numRepairPackets - 1)
     * .build();
     * </pre>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being encoded, and {@code max_esi} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum
     * value for the encoding symbol identifier}, then the following must be true, otherwise an
     * {@code IllegalArgumentException} is thrown:
     * <p>
     * <ul>
     * <li>{@code numRepairPackets} &gt; 0
     * <li>{@code numRepairPackets} &le; ({@code 1 + max_esi - K})
     * </ul>
     * 
     * @param numRepairPackets
     *            The number of repair packets to iterate
     * @return an iterable over a number of repair packets
     * @exception IllegalArgumentException
     *                If the number of repair packets is invalid
     * @see #newIterableBuilder()
     */
    public Iterable<EncodingPacket> repairPacketsIterable(int numRepairPackets);
}
