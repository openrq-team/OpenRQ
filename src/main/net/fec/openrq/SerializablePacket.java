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
package net.fec.openrq;


import java.io.Serializable;
import java.util.Objects;

import net.fec.openrq.parameters.ParameterIO;


/**
 * A serializable encoding packet.
 * <p>
 * This class serializes a <em>FEC Payload ID</em>, and an array of bytes containing encoding symbols (see class
 * {@link EncodingPacket}).
 * 
 * @see EncodingPacket
 */
public class SerializablePacket implements Serializable {

    private static final long serialVersionUID = -9061089983828792901L;

    private final int fecPayloadID;
    private final byte[] symbols;


    /**
     * Constructs a new serializable encoding packet.
     * 
     * @param fecPayloadID
     *            A <em>FEC Payload ID</em> as specified in RFC 6330
     * @param symbols
     *            The data from the encoding symbols in the packet
     * @exception NullPointerException
     *                If {@code symbols} is {@code null}
     */
    public SerializablePacket(int fecPayloadID, byte[] symbols) {

        this.fecPayloadID = fecPayloadID;
        this.symbols = Objects.requireNonNull(symbols);
    }

    /**
     * Returns the source block number of all encoding symbols.
     * 
     * @return the source block number of all encoding symbols
     */
    public int sourceBlockNumber() {

        return ParameterIO.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * Returns the encoding symbol identifier of the first encoding symbol.
     * 
     * @return the encoding symbol identifier of the first encoding symbol
     */
    public int encodingSymbolID() {

        return ParameterIO.extractEncodingSymbolID(fecPayloadID);
    }

    /**
     * Returns the encoding symbols data.
     * 
     * @return the encoding symbols data
     */
    public byte[] symbols() {

        return symbols;
    }

    // writeReplace method for the serialization proxy pattern
    private Object writeReplace() {

        return new Proxy(this);
    }

    @SuppressWarnings("unused")
    // readObject method for the serialization proxy pattern
    private void readObject(java.io.ObjectInputStream stream) throws java.io.InvalidObjectException {

        throw new java.io.InvalidObjectException("Proxy required");
    }


    // Serialization proxy
    private static final class Proxy implements Serializable {

        private static final long serialVersionUID = 7013520842258510910L;

        private final int fecPayloadID;
        private final byte[] symbols;


        Proxy(SerializablePacket packet) {

            this.fecPayloadID = packet.fecPayloadID;
            this.symbols = packet.symbols;
        }

        private Object readResolve() {

            // uses constructor in order to check for the correctness of the parameters
            return new SerializablePacket(fecPayloadID, symbols);
        }
    }
}
