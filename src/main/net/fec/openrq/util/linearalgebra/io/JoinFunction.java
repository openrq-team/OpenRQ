/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 */

package net.fec.openrq.util.linearalgebra.io;


import static net.fec.openrq.util.math.OctetOps.aMinusB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;
import net.fec.openrq.util.math.OctetOps;


abstract class JoinFunction {

    public abstract byte evaluate(byte a, byte b);


    public static final JoinFunction ADD = new JoinFunction() {

        @Override
        public byte evaluate(byte a, byte b) {

            return aPlusB(a, b);
        }
    };

    public static final JoinFunction SUB = new JoinFunction() {

        @Override
        public byte evaluate(byte a, byte b) {

            return aMinusB(a, b);
        }
    };

    public static final JoinFunction MUL = new JoinFunction() {

        @Override
        public byte evaluate(byte a, byte b) {

            return aTimesB(a, b);
        }
    };

    public static final JoinFunction DIV = new JoinFunction() {

        @Override
        public byte evaluate(byte a, byte b) {

            return OctetOps.aDividedByB(a, b);
        }
    };
}
