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
 * Contributor(s): Maxim Samoylov
 * Miron Aseev
 */
package net.fec.openrq.util.linearalgebra.vector;


import static net.fec.openrq.util.math.OctetOps.aDividedByB;
import static net.fec.openrq.util.math.OctetOps.aMinusB;
import static net.fec.openrq.util.math.OctetOps.aPlusB;
import static net.fec.openrq.util.math.OctetOps.aTimesB;
import static net.fec.openrq.util.math.OctetOps.maxByte;
import static net.fec.openrq.util.math.OctetOps.maxOfAandB;
import static net.fec.openrq.util.math.OctetOps.minByte;
import static net.fec.openrq.util.math.OctetOps.minOfAandB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Random;

import net.fec.openrq.util.io.printing.appendable.PrintableAppendable;
import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.serialize.DeserializationException;
import net.fec.openrq.util.linearalgebra.serialize.Serialization;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorPredicate;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorProcedure;
import net.fec.openrq.util.linearalgebra.vector.source.ArrayVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.LoopbackVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.RandomVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;


public final class ByteVectors {

    /**
     * Checks whether the vector is a
     * <a href="http://mathworld.wolfram.com/ZeroMatrix.html">zero
     * vector</a>.
     */
    public static final VectorPredicate ZERO_VECTOR = new VectorPredicate() {

        @Override
        public boolean test(int i, byte value) {

            return value == 0;
        }
    };


    /**
     * Creates a const function that evaluates it's argument to given {@code value}.
     * 
     * @param arg
     *            a const value
     * @return a closure object that does {@code _}
     */
    public static VectorFunction asConstFunction(final byte arg) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return arg;
            }
        };
    }

    /**
     * Creates a plus function that adds given {@code value} to it's argument.
     * 
     * @param arg
     *            a value to be added to function's argument
     * @return a closure object that does {@code _ + _}
     */
    public static VectorFunction asPlusFunction(final byte arg) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return aPlusB(value, arg);
            }
        };
    }

    /**
     * Creates a minus function that subtracts given {@code value} from it's argument.
     * 
     * @param arg
     *            a value to be subtracted from function's argument
     * @return a closure that does {@code _ - _}
     */
    public static VectorFunction asMinusFunction(final byte arg) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return aMinusB(value, arg);
            }
        };
    }

    /**
     * Creates a mul function that multiplies given {@code value} by it's argument.
     * 
     * @param arg
     *            a value to be multiplied by function's argument
     * @return a closure that does {@code _ * _}
     */
    public static VectorFunction asMulFunction(final byte arg) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return aTimesB(value, arg);
            }
        };
    }

    /**
     * Creates a div function that divides it's argument by given {@code value}.
     * 
     * @param arg
     *            a divisor value
     * @return a closure that does {@code _ / _}
     */
    public static VectorFunction asDivFunction(final byte arg) {

        return new VectorFunction() {

            @Override
            public byte evaluate(int i, byte value) {

                return aDividedByB(value, arg);
            }
        };
    }

    /**
     * Creates a sum vector accumulator that calculates the sum of all elements in the vector.
     * 
     * @param neutral
     *            the neutral value
     * @return a sum accumulator
     */
    public static VectorAccumulator asSumAccumulator(final byte neutral) {

        return new VectorAccumulator() {

            private byte result = neutral;


            @Override
            public void update(int i, byte value) {

                result = aPlusB(result, value);
            }

            @Override
            public byte accumulate() {

                byte acc = result;
                result = neutral;
                return acc;
            }
        };
    }

    /**
     * Creates a product vector accumulator that calculates the product of all elements in the vector.
     * 
     * @param neutral
     *            the neutral value
     * @return a product accumulator
     */
    public static VectorAccumulator asProductAccumulator(final byte neutral) {

        return new VectorAccumulator() {

            private byte result = neutral;


            @Override
            public void update(int i, byte value) {

                result = aTimesB(result, value);
            }

            @Override
            public byte accumulate() {

                byte acc = result;
                result = neutral;
                return acc;
            }
        };
    }

    /**
     * Makes a minimum vector accumulator that accumulates the minimum across vector elements.
     * 
     * @return a minimum vector accumulator
     */
    public static VectorAccumulator mkMinAccumulator() {

        return new VectorAccumulator() {

            private byte result = maxByte();


            @Override
            public void update(int i, byte value) {

                result = minOfAandB(result, value);
            }

            @Override
            public byte accumulate() {

                byte min = result;
                result = maxByte();
                return min;
            }
        };
    }

    /**
     * Makes a maximum vector accumulator that accumulates the maximum across vector elements.
     * 
     * @return a maximum vector accumulator
     */
    public static VectorAccumulator mkMaxAccumulator() {

        return new VectorAccumulator() {

            private byte result = minByte();


            @Override
            public void update(int i, byte value) {

                result = maxOfAandB(result, value);
            }

            @Override
            public byte accumulate() {

                byte max = result;
                result = minByte();
                return max;
            }
        };
    }

    /**
     * Creates a function accumulator, that accumulates all elements in the vector after applying given {@code function}
     * to each of them.
     * 
     * @param accumulator
     *            the accumulator
     * @param function
     *            the function
     * @return a sum function accumulator
     */
    public static VectorAccumulator asFunctionAccumulator(
        final VectorAccumulator accumulator,
        final VectorFunction function)
    {

        return new VectorAccumulator() {

            @Override
            public void update(int i, byte value) {

                accumulator.update(i, function.evaluate(i, value));
            }

            @Override
            public byte accumulate() {

                return accumulator.accumulate();
            }
        };
    }

    /**
     * Creates an accumulator procedure that adapts a vector accumulator for procedure
     * interface. This is useful for reusing a single accumulator for multiple fold operations
     * in multiple vectors.
     * 
     * @param accumulator
     *            the vector accumulator
     * @return an accumulator procedure
     */
    public static VectorProcedure asAccumulatorProcedure(final VectorAccumulator accumulator) {

        return new VectorProcedure() {

            @Override
            public void apply(int i, byte value) {

                accumulator.update(i, value);
            }
        };
    }

    /**
     * Creates a vector source of given {@code vector}.
     * 
     * @param vector
     *            the source vector
     * @return a vector source
     */
    public static VectorSource asVectorSource(ByteVector vector) {

        return new LoopbackVectorSource(vector);
    }

    /**
     * Creates an array vector source of given array {@code reference}.
     * 
     * @param array
     *            the source array
     * @return an array vector source
     */
    public static VectorSource asArraySource(byte[] array) {

        return new ArrayVectorSource(array);
    }

    /**
     * Creates a random vector source of given {@code length}.
     * 
     * @param length
     *            the length of the source
     * @param random
     * @return a random vector source
     */
    public static VectorSource asRandomSource(int length, Random random) {

        return new RandomVectorSource(length, random);
    }

    /**
     * Creates a default vector from given vararg {@code values}.
     * 
     * @param values
     *            of the vector
     * @return a default vector
     */
    public static ByteVector asVector(byte... values) {

        return LinearAlgebra.DEFAULT_FACTORY.createVector(values);
    }

    /**
     * Prints a vector to a given appendable.
     * 
     * @param vector
     *            the vector to be printed
     * @param appendable
     *            the appendable on which the vector is printed
     */
    public static void printVector(ByteVector vector, Appendable appendable) {

        final PrintableAppendable output = PrintableAppendable.of(appendable, true);
        final int N = vector.length();

        // this prints two lines, the first with indexes and the second with the actual values
        // (this only works fine for indices less than 100)
        // for (int j = 0; j < N; j++)
        // output.printf("* %02d ", j);

        // output.println('|');

        for (int j = 0; j < N; j++)
            output.printf("| %02X ", vector.get(j));
        output.println('|');
    }

    public static ByteVector deserializeVector(ByteBuffer buffer) throws DeserializationException {

        Serialization.Type type = Serialization.readType(buffer);
        switch (type) {
            case DENSE_VECTOR:
                return LinearAlgebra.DENSE_FACTORY.deserializeVector(buffer);

            case SPARSE_VECTOR:
                return LinearAlgebra.SPARSE_FACTORY.deserializeVector(buffer);

            default:
                throw new DeserializationException("serialized data does not contain a byte vector");
        }
    }

    public static ByteVector deserializeVector(ReadableByteChannel ch) throws IOException, DeserializationException {

        Serialization.Type type = Serialization.readType(ch);
        switch (type) {
            case DENSE_VECTOR:
                return LinearAlgebra.DENSE_FACTORY.deserializeVector(ch);

            case SPARSE_VECTOR:
                return LinearAlgebra.SPARSE_FACTORY.deserializeVector(ch);

            default:
                throw new DeserializationException("serialized data does not contain a byte vector");
        }
    }
}
