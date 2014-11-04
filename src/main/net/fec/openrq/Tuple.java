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


import net.fec.openrq.util.rq.Deg;
import net.fec.openrq.util.rq.Rand;
import net.fec.openrq.util.rq.SystematicIndices;


/**
 */
final class Tuple {

    private final long d, a, b, d1, a1, b1;


    Tuple(int Kprime, long X) {

        int Ki = SystematicIndices.getKIndex(Kprime);
        int S = SystematicIndices.S(Ki);
        int H = SystematicIndices.H(Ki);
        int W = SystematicIndices.W(Ki);
        int L = Kprime + S + H;
        int J = SystematicIndices.J(Ki);
        int P = L - W;
        long P1 = MatrixUtilities.ceilPrime(P);

        long A = 53591 + J * 997;
        if (A % 2 == 0) A++;

        long B = 10267 * (J + 1);

        long y = (B + X * A) % 4294967296L; // 2^^32

        long v = Rand.rand(y, 0, 1048576L); // 2^^20

        this.d = Deg.deg(v, W);
        this.a = 1 + Rand.rand(y, 1, W - 1);
        this.b = Rand.rand(y, 2, W);
        if (this.d < 4) d1 = 2 + Rand.rand(X, 3, 2L);
        else d1 = 2;
        this.a1 = 1 + Rand.rand(X, 4, P1 - 1);
        this.b1 = Rand.rand(X, 5, P1);
    }

    long getD() {

        return d;
    }

    long getA() {

        return a;
    }

    long getB() {

        return b;
    }

    long getD1() {

        return d1;
    }

    long getA1() {

        return a1;
    }

    long getB1() {

        return b1;
    }
}
