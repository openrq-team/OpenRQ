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
package net.fec.openrq.util.linearalgebra.vector.operation;


import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.vector.ByteVector;
import net.fec.openrq.util.linearalgebra.vector.operation.inplace.InPlaceVectorFromVectorSubtraction;
import net.fec.openrq.util.linearalgebra.vector.operation.inplace.InPlaceVectorToVectorAddition;
import net.fec.openrq.util.linearalgebra.vector.operation.inplace.InPlaceVectorToVectorAdditionWithMultiplier;
import net.fec.openrq.util.linearalgebra.vector.operation.ooplace.OoPlaceHadamardProduct;
import net.fec.openrq.util.linearalgebra.vector.operation.ooplace.OoPlaceVectorFromVectorSubtraction;
import net.fec.openrq.util.linearalgebra.vector.operation.ooplace.OoPlaceVectorToVectorAddition;


public final class VectorOperations {

    public static VectorVectorOperation<Void> inPlaceVectorToVectorAddition() {

        return new InPlaceVectorToVectorAddition();
    }

    public static VectorVectorOperation<Void> inPlaceVectorToVectorAddition(int from, int to) {

        return new InPlaceVectorToVectorAddition(from, to);
    }

    public static VectorVectorOperation<Void> inPlaceVectorToVectorAdditionWithMultiplier(byte bMultiplier) {

        return new InPlaceVectorToVectorAdditionWithMultiplier(bMultiplier);
    }

    public static VectorVectorOperation<Void> inPlaceVectorToVectorAdditionWithMultiplier(
        byte bMultiplier,
        int from,
        int to)
    {

        return new InPlaceVectorToVectorAdditionWithMultiplier(bMultiplier, from, to);
    }

    public static VectorVectorOperation<ByteVector> ooPlaceVectorToVectorAddition(Factory factory) {

        return new OoPlaceVectorToVectorAddition(factory);
    }

    public static VectorVectorOperation<ByteVector> ooPlaceHadamardProduct(Factory factory) {

        return new OoPlaceHadamardProduct(factory);
    }

    public static VectorVectorOperation<ByteVector> ooPlaceVectorFromVectorSubtraction(Factory factory) {

        return new OoPlaceVectorFromVectorSubtraction(factory);
    }

    public static VectorVectorOperation<Void> inPlaceVectorFromVectorSubtraction() {

        return new InPlaceVectorFromVectorSubtraction();
    }
}
