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

/**
 * An enum value indicating the result of a source block decoder receiving an encoding packet.
 */
public enum SourceBlockState {

    /**
     * Result value indicating that not enough encoding symbols are available for a decoding operation.
     */
    INCOMPLETE,

    /**
     * Result value indicating that a decoding operation took place and succeeded in decoding the source block.
     */
    DECODED,

    /**
     * Result value indicating that a decoding operation took place but failed in decoding the source block.
     */
    DECODING_FAILURE
}
