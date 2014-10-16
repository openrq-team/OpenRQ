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
 */
public final class Deg {

    public static long deg(long v, long W) {

        int i;
        for (i = 0; i < 31; i++) {
            if (v < table[i]) break;
        }

        if (i == 31) throw new RuntimeException("Inconsistent table state");

        return (Math.min(i, W - 2));
    }


    private static long[] table = {
                                   0,
                                   5243,
                                   529531,
                                   704294,
                                   791675,
                                   844104,
                                   879057,
                                   904023,
                                   922747,
                                   937311,
                                   948962,
                                   958494,
                                   966438,
                                   973160,
                                   978921,
                                   983914,
                                   988283,
                                   992138,
                                   995565,
                                   998631,
                                   1001391,
                                   1003887,
                                   1006157,
                                   1008229,
                                   1010129,
                                   1011876,
                                   1013490,
                                   1014983,
                                   1016370,
                                   1017662,
                                   1048576
    };


    private Deg() {

        // not instantiable
    }
}
