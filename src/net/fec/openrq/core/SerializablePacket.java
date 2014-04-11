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
package net.fec.openrq.core;


import java.io.Serializable;
import java.util.Objects;

import net.fec.openrq.core.parameters.ParameterIO;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
public class SerializablePacket implements Serializable {

    private static final long serialVersionUID = -9061089983828792901L;

    private final int fecPayloadID;
    private final byte[] symbols;


    /**
     * @param fecPayloadID
     * @param symbols
     */
    SerializablePacket(int fecPayloadID, byte[] symbols) {

        this.fecPayloadID = fecPayloadID;
        this.symbols = Objects.requireNonNull(symbols);
    }

    /**
     * @return
     */
    int sbn() {

        return ParameterIO.extractSourceBlockNumber(fecPayloadID);
    }

    /**
     * @return
     */
    int esi() {

        return ParameterIO.extractEncodingSymbolID(fecPayloadID);
    }

    /**
     * @return
     */
    byte[] symbols() {

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
