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


import net.fec.openrq.core.parameters.ParameterChecker;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface SourceBlockEncoder {

    /**
     * Returns the source block number for the source block currently being encoded.
     * 
     * @return the source block number for the source block currently being encoded
     */
    public int sourceBlockNumber();

    /**
     * Returns the number of source symbols from the source block currently being encoded.
     * 
     * @return the number of source symbols from the source block currently being encoded
     */
    public int numberOfSourceSymbols();

    /**
     * Returns an encoding packet with a source symbol from the source block currently being encoded.
     * <p>
     * More specifically, if we have
     * <ul>
     * <li>{@code SBN} as the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} as the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a source symbol identified by {@code (SBN, ESI)}.
     * <p>
     * If we have
     * <ul>
     * <li>{@code K} as the number of source symbols from the source block currently being encoded,
     * </ul>
     * then the encoding symbol identifier ({@code ESI}) is only valid if {@code (ESI >= 0 && ESI < K)}.
     * <p>
     * 
     * @param esi
     *            The encoding symbol identifier of the source symbol in the returned packet
     * @return an encoding packet with a source symbol from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getSourcePacket(int esi);

    /**
     * Returns an encoding packet with multiple source symbols from the source block currently being encoded.
     * <p>
     * More specifically, if we have
     * <ul>
     * <li>{@code SBN} as the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} as the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a first source symbol identified by {@code (SBN, ESI)}, a second
     * symbol identified by {@code (SBN, ESI+1)}, a third symbol identified by {@code (SBN, ESI+2)}, etc.
     * <p>
     * If we have
     * <ul>
     * <li>{@code K} as the number of source symbols from the source block currently being encoded,
     * </ul>
     * then the encoding symbol identifier ({@code ESI}) is only valid if {@code (ESI >= 0 && ESI < K)}.
     * <p>
     * Additionally, the number of symbols ({@code S}) is only valid if {@code (S > 0 && S <= (K - ESI))}.
     * <p>
     * 
     * @param esi
     *            The encoding symbol identifier of the first source symbol in the returned packet
     * @param numSymbols
     *            The number of source symbols to be placed in the returned packet
     * @return an encoding packet with multiple source symbols from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getSourcePacket(int esi, int numSymbols);

    /**
     * Returns an encoding packet with a repair symbol from the source block currently being encoded.
     * <p>
     * More specifically, if we have
     * <ul>
     * <li>{@code SBN} as the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} as the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a repair symbol identified by {@code (SBN, ESI)}.
     * <p>
     * If we have
     * <ul>
     * <li>{@code K} as the number of source symbols from the source block currently being encoded,
     * <li>{@code MAX_ESI} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the encoding
     * symbol identifier},</li>
     * </ul>
     * then the encoding symbol identifier ({@code ESI}) is only valid if {@code (ESI >= K && ESI <= MAX_ESI)}.
     * <p>
     * 
     * @param esi
     *            The encoding symbol identifier of the repair symbol in the returned packet
     * @return an encoding packet with a repair symbol from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier is invalid
     * @see #sourceBlockNumber()
     */
    public EncodingPacket getRepairPacket(int esi);

    /**
     * Returns an encoding packet with multiple repair symbols from the source block currently being encoded.
     * <p>
     * More specifically, if we have
     * <ul>
     * <li>{@code SBN} as the source block number for the source block currently being encoded,</li>
     * <li>{@code ESI} as the provided encoding symbol identifier,</li>
     * </ul>
     * then this method returns an encoding packet with a first repair symbol identified by {@code (SBN, ESI)}, a second
     * symbol identified by {@code (SBN, ESI+1)}, a third symbol identified by {@code (SBN, ESI+2)}, etc.
     * <p>
     * If we have
     * <ul>
     * <li>{@code K} as the number of source symbols from the source block currently being encoded,
     * <li>{@code MAX_ESI} as the {@linkplain ParameterChecker#maxEncodingSymbolID() maximum value for the encoding
     * symbol identifier},</li>
     * </ul>
     * then the encoding symbol identifier ({@code ESI}) is only valid if {@code (ESI >= K && ESI <= MAX_ESI)}.
     * <p>
     * Additionally, the number of symbols ({@code S}) is only valid if {@code (S > 0 && S <= (1 + MAX_ESI - ESI))}.
     * <p>
     * 
     * @param esi
     *            The encoding symbol identifier of the first repair symbol in the returned packet
     * @param numSymbols
     *            The number of repair symbols to be placed in the returned packet
     * @return an encoding packet with multiple repair symbols from the source block currently being encoded
     * @exception IllegalArgumentException
     *                If the provided encoding symbol identifier or the number of symbols are invalid
     * @see #sourceBlockNumber()
     * @see #numberOfSourceSymbols()
     */
    public EncodingPacket getRepairPacket(int esi, int numSymbols);
}
