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

package net.fec.openrq.parameters;

/**
 */
final class InternalConstants {

    static final int Al = 1;

    static final int K_max = 56_403;                 // "maximum number of symbols in each source block"
    static final int Z_max = 256;                    // "maximum number of source blocks"
    static final int Kt_max = K_max * Z_max;         // "maximum number of symbols"
    static final int T_max = (65_535 / Al) * Al;     // "maximum symbol size, in octets"
    static final long F_max = (long)Kt_max * T_max;  // "maximum transfer length of the object, in octets"
    static final int N_max = 1/* K_max */;           // "maximum interleaver length, in number of sub-blocks"
    // TODO enable interleaving

    static final int K_min = 1;
    static final int K_prime_min = 10;  // the first K' value in the systematic indices table
    static final int Z_min = 1;
    static final int T_min = Al;
    static final long F_min = 1L; // RFC 6330 defines F as a non-negative value, but we force a positive value here
    static final int N_min = 1;

    static final int SBN_max = 255;
    static final int ESI_max = 16_777_215;

    static final int SBN_min = 0;
    static final int ESI_min = 0;

    static final int F_num_bytes = 5;
    static final int ESI_num_bytes = 3;

    static final long common_OTI_reserved_inverse_mask = 0xFF_FF_FF_FF_FF_00_FF_FFL; // third octet is reserved bits
}
