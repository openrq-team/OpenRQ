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
package net.fec.openrq;


import java.util.Objects;


/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class EncodingSymbol {

    private final PaddedByteVector data;
    private final FECPayloadID fecPayloadID;


    EncodingSymbol(PaddedByteVector data, FECPayloadID fecPayloadID) {

        this.data = Objects.requireNonNull(data);
        this.fecPayloadID = Objects.requireNonNull(fecPayloadID);
    }

    PaddedByteVector getData() {

        return data;
    }

    FECPayloadID getFECPayloadID() {

        return fecPayloadID;
    }

    int getSBN() {

        return fecPayloadID.sourceBlockNumber();
    }

    int getESI() {

        return fecPayloadID.encodingSymbolID();
    }

    int getISI(int K) {

        int kLinha = SystematicIndices.ceil(K);

        return getESI() + (kLinha - K); // yes, I know its commutative: it's just for a better code reading experience.
    }
}
