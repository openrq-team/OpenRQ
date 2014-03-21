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

package net.fec.openrq.core.parameters;

/**
 * @author Jos&#233; Lopes &lt;jlopes&#064;lasige.di.fc.ul.pt&gt;
 * @author Ricardo Fonseca &lt;ricardof&#064;lasige.di.fc.ul.pt&gt;
 */
final class InternalConstants {

    // == K == //
    static final int K_MIN = 1;
    static final int K_MAX = 56403;

    // == data length == //
    // the RFC specifies a non-negative value, but we force a positive value here
    static final long MIN_F = 1L;
    static final long MAX_F = 946270874880L;
    static final int NUM_BYTES_F = 5;

    // == symbol size == //
    static final int MIN_T = 1;
    static final int MAX_T = (1 << 16) - 1;

    // == number of source blocks == //
    static final int MIN_Z = 1;
    static final int MAX_Z = 1 << 8;

    // == number of sub-blocks == //
    static final int MIN_N = 1;
    static final int MAX_N = 1/* K_MAX */; // TODO enable interleaving

    // == symbol alignment == //
    static final int ALIGN_VALUE = 1;

    // == source block number == //
    static final int MIN_SBN = 0;
    static final int MAX_SBN = (1 << 8) - 1;

    // == encoding symbol identifier == //
    static final int MIN_ESI = 0;
    static final int MAX_ESI = (1 << 24) - 1;
    static final int NUM_BYTES_ESI = 3;
}
