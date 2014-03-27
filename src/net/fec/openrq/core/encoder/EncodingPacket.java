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


import java.nio.ByteBuffer;
import java.util.List;

import net.fec.openrq.core.FECPayloadID;
import net.fec.openrq.core.SymbolType;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public interface EncodingPacket {

    /**
     * Returns the FEC payload identifier associated to this encoding packet.
     * 
     * @return the FEC payload identifier associated to this encoding packet
     */
    public FECPayloadID fecPayloadID();

    /**
     * Returns the data from the symbol(s) in this encoding packet. The returned symbol(s) have contiguous <i>encoding
     * symbol identifiers</i>.
     * <p>
     * The returned list of buffers is immutable. Each buffer contains one distinct symbol and is
     * {@linkplain ByteBuffer#isReadOnly() read-only}. Additionally, each buffer has initially a
     * {@linkplain ByteBuffer#position() position} of 0, a {@linkplain ByteBuffer#capacity() capacity} equal to the size
     * of a symbol, and a {@linkplain ByteBuffer#limit() limit} equal to the capacity. Changes in the current positions
     * or limits of the buffers will persist.
     * 
     * @return an immutable list of one or more read-only buffers with the data from the symbol(s) in this packet
     */
    public List<ByteBuffer> getSymbolData();

    /**
     * Returns the number of symbols in this encoding packet. This value is the same as {@code getSymbolData().size()}.
     * 
     * @return the number of symbols in this encoding packet
     */
    public int numberOfSymbols();

    /**
     * Returns the type of all the symbols in this encoding packet.
     * 
     * @return the type of all the symbols in this encoding packet
     */
    public SymbolType symbolType();
}
