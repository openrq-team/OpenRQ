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
package net.fec.openrq.util.rq;

/**
 * Decodes intermediate symbols from a set of source symbols of an extended source block.
 */
public interface IntermediateSymbolsDecoder {

    /**
     * Returns the supported value of K' (see RFC 6330).
     * 
     * @return the supported value of K'
     */
    public int supportedKPrime();

    /**
     * Decodes intermediate symbols from a set of source symbols of an extended source block.
     * 
     * @param D
     * @return the intermediate symbols
     */
    public byte[][] decode(byte[][] D);
}
