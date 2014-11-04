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


import net.fec.openrq.util.math.ExtraMath;


/**
 */
final class Partition {

    private final int IL;
    private final int IS;
    private final int JL;
    private final int JS;


    // requires I > 0 && J > 0
    Partition(int I, int J) {

        this.IL = ExtraMath.ceilDiv(I, J);
        this.IS = I / J; // floor(I / J)
        this.JL = I - (IS * J);
        this.JS = J - JL;
    }

    int get(int i) {

        switch (i) {
            case 1:
                return IL;
            case 2:
                return IS;
            case 3:
                return JL;
            case 4:
                return JS;
            default:
                throw new IllegalArgumentException("Argument must be from 1 to 4.");
        }
    }
}
