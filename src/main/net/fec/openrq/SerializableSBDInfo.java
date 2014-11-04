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


/**
 * A serializable container for source block decoder information.
 * <p>
 * This class serializes the information defined in class {@link SBDInfo}.
 */
public class SerializableSBDInfo implements Serializable {

    private static final long serialVersionUID = -8860883442339675710L;

    private final byte[] sbdInfo;


    /**
     * Constructs a new serializable object containing a source block decoder information.
     * 
     * @param sbdInfo
     *            The information encoded as an array of bytes
     * @exception NullPointerException
     *                If {@code sbdInfo} is {@code null}
     */
    public SerializableSBDInfo(byte[] sbdInfo) {

        this.sbdInfo = Objects.requireNonNull(sbdInfo);
    }

    /**
     * Returns the source block decoder information, encoded as an array of bytes.
     * 
     * @return the source block decoder information, encoded as an array of bytes
     */
    public byte[] sourceBlockDecoderInfo() {

        return sbdInfo;
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

        private static final long serialVersionUID = -5774803485149469364L;

        private final byte[] sbdInfo;


        Proxy(SerializableSBDInfo info) {

            this.sbdInfo = info.sbdInfo;
        }

        private Object readResolve() {

            // uses constructor in order to check for the correctness of the parameters
            return new SerializableSBDInfo(sbdInfo);
        }
    }
}
