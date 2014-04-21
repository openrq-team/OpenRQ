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
 * A source block can be encoded independently by an instance of this interface, and the block is identified by a source
 * block number, which is carried inside an encoding packet. The method {@link #sourceBlockNumber()} provides the source
 * block number that identifies the source block being encoded. Additionally, the number of source symbols into which
 * the source block is divided is given by the method {@link #numberOfSourceSymbols()}.
 * <p>
 * The method {@link #getSourcePacket(int)} returns an encoding packet with a specific source symbol, and the method
 * {@link #getRepairPacket(int)} returns an encoding packet with a specific repair symbol. There are also methods that
 * return encoding packets with multiple source/repair symbols within.
 */
public interface SourceBlockEncoder {

    /**
     * Returns the data encoder object from which this source block encoder was retrieved.
     * 
     * @return the data encoder object from which this source block encoder was retrieved
     */
    public DataEncoder dataEncoder();

    /**
     * Returns the source block number for the source block being encoded.
     * 
     * @return the source block number for the source block being encoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the total number of source symbols into which is divided the source block being encoded.
     * 
     * @return the total number of source symbols into which is divided the source block being encoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns an encoding packet with a source symbol from the source block being encoded.
     * <p>
     * More specifically, if we have <b>sbn</b> as the source block number for the source block being encoded, then this
     * method returns an encoding packet with a source symbol identified by <b>&lt;sbn, esi&gt;</b>.
     * <p>
     * Additionally, if we have <b>K</b> as the number of source symbols into which is divided the source block being
     * encoded, then the following must be true, otherwise an {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li><b>esi</b> &ge; 0
     * <li><b>esi</b> &lt; <b>K</b>
     * </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the source symbol in the returned packet
     * @return an encoding packet with a source symbol from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getSourcePacket(int esi);

    /**
     * Returns an encoding packet with multiple source symbols from the source block being encoded.
     * <p>
     * More specifically, if we have <b>sbn</b> as the source block number for the source block being encoded, then this
     * method returns an encoding packet with:
     * <ul>
     * <li>a 1st source symbol identified by <b>&lt;sbn, esi&gt;</b>,
     * <li>a 2nd source symbol identified by <b>&lt;sbn, esi+1&gt;</b>,
     * <li>a 3rd source symbol identified by <b>&lt;sbn, esi+2&gt;</b>,
     * <li>etc.
     * </ul>
     * <p>
     * Additionally, if we have <b>K</b> as the number of source symbols into which is divided the source block being
     * encoded, then the following must be true, otherwise an {@code IllegalArgumentException} is thrown:
     * <ul>
     * <li><b>esi</b> &ge; 0
     * <li><b>esi</b> &lt; <b>K</b>
     * <li><b>numSymbols</b> &gt; 0
     * <li><b>numSymbols</b> &le; (<b>K</b> - <b>esi</b>)
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
    public EncodingPacket getSourcePacket(int esi, int numSymbols);

    /**
     * Returns an encoding packet with a repair symbol from the source block being encoded.
     * <p>
     * More specifically, if we have <b>sbn</b> as the source block number for the source block being encoded, then this
     * method returns an encoding packet with a repair symbol identified by <b>&lt;sbn, esi&gt;</b>.
     * <p>
     * Additionally, if we have <b>K</b> as the number of source symbols into which is divided the source block being
     * encoded, and <b>max_esi</b> as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the
     * encoding symbol identifier}, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li><b>esi</b> &ge; K
     * <li><b>esi</b> &le; <b>max_esi</b>
     * </ul>
     * 
     * @param esi
     *            The encoding symbol identifier of the repair symbol in the returned packet
     * @return an encoding packet with a repair symbol from the source block being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getRepairPacket(int esi);

    /**
     * Returns an encoding packet with multiple repair symbols from the source block being encoded.
     * <p>
     * More specifically, if we have <b>sbn</b> as the source block number for the source block being encoded, then this
     * method returns an encoding packet with:
     * <ul>
     * <li>a 1st repair symbol identified by <b>&lt;sbn, esi&gt;</b>,
     * <li>a 2nd repair symbol identified by <b>&lt;sbn, esi+1&gt;</b>,
     * <li>a 3rd repair symbol identified by <b>&lt;sbn, esi+2&gt;</b>,
     * <li>etc.
     * </ul>
     * <p>
     * Additionally, if we have <b>K</b> as the number of source symbols into which is divided the source block being
     * encoded, and <b>max_esi</b> as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the
     * encoding symbol identifier}, then the following must be true, otherwise an {@code IllegalArgumentException} is
     * thrown:
     * <ul>
     * <li><b>esi</b> &ge; K
     * <li><b>esi</b> &le; <b>max_esi</b>
     * <li><b>numSymbols</b> &gt; 0
     * <li><b>numSymbols</b> &le; (1 + <b>max_esi</b> - <b>esi</b>)
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
    public EncodingPacket getRepairPacket(int esi, int numSymbols);
}
