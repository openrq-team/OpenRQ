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


import net.fec.openrq.util.linearalgebra.vector.dense.DenseByteVector;
import net.fec.openrq.util.linearalgebra.vector.sparse.SparseByteVector;


public abstract class VectorVectorOperation<T> {

    public abstract T apply(final SparseByteVector a, final SparseByteVector b);

    public abstract T apply(final SparseByteVector a, final DenseByteVector b);

    public abstract T apply(final DenseByteVector a, final DenseByteVector b);

    public abstract T apply(final DenseByteVector a, final SparseByteVector b);

    public VectorOperation<T> curry(final SparseByteVector a) {

        return new VectorOperation<T>() {

            @Override
            public T apply(final SparseByteVector b) {

                return VectorVectorOperation.this.apply(a, b);
            }

            @Override
            public T apply(final DenseByteVector b) {

                return VectorVectorOperation.this.apply(a, b);
            }
        };
    }

    public VectorOperation<T> curry(final DenseByteVector a) {

        return new VectorOperation<T>() {

            @Override
            public T apply(final SparseByteVector b) {

                return VectorVectorOperation.this.apply(a, b);
            }

            @Override
            public T apply(final DenseByteVector b) {

                return VectorVectorOperation.this.apply(a, b);
            }
        };
    }
}
