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

package net.fec.openrq.decoder;


import java.util.Set;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.SBDInfo;
import net.fec.openrq.parameters.ParameterChecker;


/**
 * A decoder for a source block.
 * <p>
 * A source block decoder is retrieved from a {@link DataDecoder} object, which is associated to some source data.
 * Source data is divided into source blocks and each source block is independently encoded by a RaptorQ encoder (as
 * specified in RFC 6330). Each source block is further divided into source symbols, which together with repair symbols
 * (extra encoded data) form the <em>encoding symbols</em>. The encoding symbols are transmitted inside encoding packets
 * to specific source blocks of the data being decoded.
 * <p>
 * A source block can be decoded independently by an instance of {@code SourceBlockDecoder}, and the block is identified
 * by a source block number, which is carried inside an encoding packet. The method {@link #sourceBlockNumber()}
 * provides the source block number that identifies the source block being decoded. Additionally, the number of source
 * symbols into which the source block is divided is given by the method {@link #numberOfSourceSymbols()}.
 * <p>
 * The method {@link #putEncodingPacket(EncodingPacket)} receives an encoding packet as argument and stores the encoding
 * symbols inside it for future decoding. If at the time the method is called, enough symbols are available for decoding
 * the source block (see "symbol overhead" below), then a decoding operation takes place which either succeeds or not (a
 * decoding failure).
 * <p>
 * Handling decoding failures is a task for the user. Typically, the user requests the sender for any missing source
 * symbols or simply waits for more encoding symbols (source or repair) to be available. The method
 * {@link #missingSourceSymbols()} returns a set with the identifiers of all missing source symbols, and the method
 * {@link #availableRepairSymbols()} returns a set with the identifiers of all available repair symbols so far.
 * <p>
 * <a name="symbol-overhead">
 * <h5>Symbol overhead</h5></a>
 * <p>
 * Imagine a source block being divided into {@code K} source symbols. Let {@code N} be the number of received encoding
 * symbols (source or repair) so far.
 * <p>
 * If all {@code K} source symbols are received then the decoding is immediate. When that is not the case, the decoder
 * will try to fill in the gaps of the missing source symbols with the received repair symbols. Whichever the case, the
 * decoder requires at least {@code N = K} encoding symbols in order to try recovering the source data.
 * <p>
 * However, {@code K} encoding symbols may not be sufficient for a successful decoding when some of those are repair
 * symbols (RaptorQ is a probabilistic code). To increase the probability of successful decoding in this case, a source
 * block decoder may be configured to start the decoding process only when it has received {@code N > K} encoding
 * symbols. The higher {@code N} is, the higher the probability. We call the {@code N - K} symbols the <b>symbol
 * overhead</b>.
 * <p>
 * The method {@link #symbolOverhead()} returns the current symbol overhead value, and the method
 * {@link #setSymbolOverhead(int)} changes that value.
 * <p>
 * Below are example symbol overhead values that allow a successful decoding with a specific probability given a number
 * of encoding symbols <em>(the probability values only apply if some of the encoding symbols are repair symbols)<em>:
 * <blockquote>
 * <table summary="Probability of successful decoding for different values of symbol overhead">
 * <tr>
 * <th align="left">Overhead</th>
 * <th align="left">Encoding symbols</th>
 * <th align="left">Probability</th>
 * </tr>
 * <tr>
 * <td><code>0</code></td>
 * <td><code>K</code></td>
 * <td>99%</td>
 * </tr>
 * <tr>
 * <td><code>1</code></td>
 * <td><code>K + 1</code></td>
 * <td>99.99%</td>
 * </tr>
 * <tr>
 * <td><code>2</code></td>
 * <td><code>K + 2</code></td>
 * <td>99.9999% <em>(one in a million chance of failure)</em> </td> </tr> </table> </blockquote>
 */
public interface SourceBlockDecoder {

    /**
     * Returns the data decoder object from which this source block decoder was retrieved.
     * 
     * @return the data decoder object from which this source block decoder was retrieved
     */
    public DataDecoder dataDecoder();

    /**
     * Returns the identifier of the source block being decoded.
     * 
     * @return the identifier of the source block being decoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the total number of source symbols into which is divided the source block being decoded.
     * 
     * @return the total number of source symbols into which is divided the source block being decoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns {@code true} if, and only if, this decoder contains the source symbol with the provided encoding symbol
     * identifier.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being decoded, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li>{@code esi} &ge; 0
     * <li>{@code esi} &lt; {@code K} </ul>
     * 
     * @param esi
     *            An encoding symbol identifier for a specific source symbol
     * @return {@code true} if, and only if, this decoder contains the specified source symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #numberOfSourceSymbols()
     */
    public boolean containsSourceSymbol(int esi);

    /**
     * Returns {@code true} if, and only if, this decoder contains the repair symbol with the provided encoding symbol
     * identifier.
     * <p>
     * The method returns {@code false} when the source block is already {@linkplain #isSourceBlockDecoded decoded}.
     * <p>
     * <b><em>Bounds checking</em></b> - If we have {@code K} as the number of source symbols into which is divided the
     * source block being decoded, and {@code max_esi} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum
     * value for the encoding symbol identifier}, then the following must be true, otherwise an
     * {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li>{@code esi} &ge; {@code K} <li>{@code esi} &le; {@code max_esi} </ul>
     * 
     * @param esi
     *            An encoding symbol identifier for a specific repair symbol
     * @return {@code true} if, and only if, this decoder contains the specified repair symbol
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #numberOfSourceSymbols()
     */
    public boolean containsRepairSymbol(int esi);

    /**
     * Returns {@code true} if, and only if, the source block being decoded is fully decoded. A source block is
     * considered fully decoded when it contains all of its source symbols.
     * 
     * @return {@code true} if, and only if, the source block being decoded is fully decoded
     * @see #containsSourceSymbol(int)
     */
    public boolean isSourceBlockDecoded();

    /**
     * Returns the latest state of this decoder. This state is updated by calling the method
     * {@link #putEncodingPacket(EncodingPacket)}.
     * <p>
     * The result of this method invocation is a {@link SourceBlockState} value:
     * <dl>
     * <dt>{@link SourceBlockState#INCOMPLETE INCOMPLETE}:</dt>
     * <dd>means that not enough encoding symbols are available for a decoding operation.</dd>
     * <dt>{@link SourceBlockState#DECODED DECODED}:</dt>
     * <dd>means that a decoding operation took place and succeeded in decoding the source block.</dd>
     * <dt>{@link SourceBlockState#DECODING_FAILURE DECODING_FAILURE}:</dt>
     * <dd>means that a decoding operation took place but failed in decoding the source block; additional encoding
     * symbols are required for a successful decoding.</dd>
     * </dl>
     * <p>
     * The latest state of a newly created decoder is always {@code INCOMPLETE}.
     * 
     * @return the latest state of this decoder
     */
    public SourceBlockState latestState();

    /**
     * Returns a set of integers containing the encoding symbol identifiers of the missing source symbols from the
     * source block being decoded. The returned set has an iteration ordering of ascending encoding symbol identifiers.
     * 
     * @return a set of encoding symbol identifiers of missing source symbols
     */
    public Set<Integer> missingSourceSymbols();

    /**
     * Returns a set of integers containing the encoding symbol identifiers of the available repair symbols for
     * decoding. If the source block is already decoded, then an immutable empty set is returned instead.
     * <p>
     * The returned set iteration follows the order by which repair symbols have been received.
     * 
     * @return a set of encoding symbol identifiers of available repair symbols, or an immutable empty set if the source
     *         block is already decoded
     */
    public Set<Integer> availableRepairSymbols();

    /**
     * Returns current information from this decoder inside an {@code SBDInfo} object. The information will consist
     * of the {@linkplain #sourceBlockNumber() source block number}, the {@linkplain #latestState() latest state}, the
     * {@linkplain #missingSourceSymbols() set of identifiers of missing source symbols}, and the
     * {@linkplain #availableRepairSymbols() set of identifiers of available repair symbols}.
     * 
     * @return current information from this decoder inside an {@code SBDInfo} object
     */
    public SBDInfo information();

    /**
     * Receives an encoded packet containing encoding symbols for the source block being decoded. If enough symbols
     * (source and repair) are available, then a decoding operation takes place.
     * <p>
     * The result of this method invocation is a {@link SourceBlockState} value:
     * <dl>
     * <dt>{@link SourceBlockState#INCOMPLETE INCOMPLETE}:</dt>
     * <dd>means that not enough encoding symbols are available for a decoding operation.</dd>
     * <dt>{@link SourceBlockState#DECODED DECODED}:</dt>
     * <dd>means that a decoding operation took place and succeeded in decoding the source block.</dd>
     * <dt>{@link SourceBlockState#DECODING_FAILURE DECODING_FAILURE}:</dt>
     * <dd>means that a decoding operation took place but failed in decoding the source block; additional encoding
     * symbols are required for a successful decoding.</dd>
     * </dl>
     * 
     * @param packet
     *            An encoding packet containing encoding symbols associated to the source block being decoded
     * @return a {@code SourceBlockState} value indicating the result of the method invocation (see method description)
     * @exception IllegalArgumentException
     *                If {@code packet.sourceBlockNumber() != this.sourceBlockNumber()}
     */
    public SourceBlockState putEncodingPacket(EncodingPacket packet);

    /**
     * Returns the current repair symbol overhead. For information on this value, refer to the section on
     * <a href="#symbol-overhead"><em>Symbol overhead</em></a> in the class header.
     * <p>
     * <b>Note</b>: the repair symbol overhead never exceeds {@link ParameterChecker#numRepairSymbolsPerBlock(int)
     * ParameterChecker.numRepairSymbolsPerBlock(K)}, where {@code K} is the {@linkplain #numberOfSourceSymbols() number
     * of source symbols}.
     * 
     * @return the current repair symbol overhead
     */
    public int symbolOverhead();

    /**
     * Sets the current symbol overhead to the specified value. For information on this value, refer to the section on
     * <a href="#symbol-overhead"><em>Symbol overhead</em></a> in the class header.
     * <p>
     * <b>Note</b>: if the specified value exceeds {@link ParameterChecker#numRepairSymbolsPerBlock(int)
     * ParameterChecker.numRepairSymbolsPerBlock(K)}, where {@code K} is the {@linkplain #numberOfSourceSymbols() number
     * of source symbols}, then the current symbol overhead will be set to that value.
     * 
     * @param symbOver
     *            A number of extra repair symbols (must be non-negative)
     * @exception IllegalArgumentException
     *                If the specified repair symbol overhead is negative
     * @see #symbolOverhead()
     */
    public void setSymbolOverhead(int symbOver);
}
