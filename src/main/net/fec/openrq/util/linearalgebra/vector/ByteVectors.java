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

/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
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


import static net.fec.openrq.util.linearalgebra.ByteOps.aDividedByB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aIsEqualToB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aMinusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aPlusB;
import static net.fec.openrq.util.linearalgebra.ByteOps.aTimesB;
import static net.fec.openrq.util.linearalgebra.ByteOps.maxByte;
import static net.fec.openrq.util.linearalgebra.ByteOps.maxOfAandB;
import static net.fec.openrq.util.linearalgebra.ByteOps.minByte;
import static net.fec.openrq.util.linearalgebra.ByteOps.minOfAandB;

import java.io.IOException;

import net.fec.openrq.util.linearalgebra.LinearAlgebra;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorPredicate;
import net.fec.openrq.util.linearalgebra.vector.source.ArrayVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.LoopbackVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.RandomVectorSource;
import net.fec.openrq.util.linearalgebra.vector.source.VectorSource;
import net.fec.openrq.util.printing.appendable.PrintableAppendable;



public final class ByteVectors {

    private static class ZeroVectorPredicate implements VectorPredicate {

        @Override
        public boolean test(int i, byte value) {

            return aIsEqualToB(value, (byte)0);
        }
    }

    private static class ConstVectorFunction implements VectorFunction {

        private byte arg;


        public ConstVectorFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, byte value) {

            return arg;
        }
    }

    private static class PlusFunction implements VectorFunction {

        private byte arg;


        public PlusFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, byte value) {

            return aPlusB(value, arg);
        }
    }

    private static class MinusFunction implements VectorFunction {

        private byte arg;


        public MinusFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, byte value) {

            return aMinusB(value, arg);
        }
    }

    private static class MulFunction implements VectorFunction {

        private byte arg;


        public MulFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, byte value) {

            return aTimesB(value, arg);
        }
    }

    private static class DivFunction implements VectorFunction {

        private byte arg;


        public DivFunction(byte arg) {

            this.arg = arg;
        }

        @Override
        public byte evaluate(int i, byte value) {

            return aDividedByB(value, arg);
        }
    }

    private static class SumVectorAccumulator implements VectorAccumulator {

        private final byte neutral;
        private byte result;


        public SumVectorAccumulator(byte neutral) {

            this.neutral = neutral;
            this.result = neutral;
        }

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
    }

    private static class ProductVectorAccumulator implements VectorAccumulator {

        private final byte neutral;
        private byte result;


        public ProductVectorAccumulator(byte neutral) {

            this.neutral = neutral;
            this.result = neutral;
        }

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
    }

    private static class FunctionVectorAccumulator implements VectorAccumulator {

        private VectorAccumulator accumulator;
        private VectorFunction function;


        public FunctionVectorAccumulator(VectorAccumulator accumulator,
            VectorFunction function) {

            this.accumulator = accumulator;
            this.function = function;
        }

        @Override
        public void update(int i, byte value) {

            accumulator.update(i, function.evaluate(i, value));
        }

        @Override
        public byte accumulate() {

            return accumulator.accumulate();
        }
    }

    private static class MinVectorAccumulator implements VectorAccumulator {

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
    }

    private static class MaxVectorAccumulator implements VectorAccumulator {

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
    }


    /**
     * Creates a const function that evaluates it's argument to given {@code value}.
     * 
     * @param value
     *            a const value
     * @return a closure object that does {@code _}
     */
    public static VectorFunction asConstFunction(byte value) {

        return new ConstVectorFunction(value);
    }

    /**
     * Creates a plus function that adds given {@code value} to it's argument.
     * 
     * @param value
     *            a value to be added to function's argument
     * @return a closure object that does {@code _ + _}
     */
    public static VectorFunction asPlusFunction(byte value) {

        return new PlusFunction(value);
    }

    /**
     * Creates a minus function that subtracts given {@code value} from it's argument.
     * 
     * @param value
     *            a value to be subtracted from function's argument
     * @return a closure that does {@code _ - _}
     */
    public static VectorFunction asMinusFunction(byte value) {

        return new MinusFunction(value);
    }

    /**
     * Creates a mul function that multiplies given {@code value} by it's argument.
     * 
     * @param value
     *            a value to be multiplied by function's argument
     * @return a closure that does {@code _ * _}
     */
    public static VectorFunction asMulFunction(byte value) {

        return new MulFunction(value);
    }

    /**
     * Creates a div function that divides it's argument by given {@code value}.
     * 
     * @param value
     *            a divisor value
     * @return a closure that does {@code _ / _}
     */
    public static VectorFunction asDivFunction(byte value) {

        return new DivFunction(value);
    }


    /**
     * Checks whether the vector is a
     * <a href="http://mathworld.wolfram.com/ZeroMatrix.html">zero
     * vector</a>.
     */
    public static final VectorPredicate ZERO_VECTOR = new ZeroVectorPredicate();


    /**
     * Creates a singleton 1-length vector of given {@code value}.
     * 
     * @param value
     *            the vector's singleton value
     * @return a singleton vector
     */
    public static ByteVector asSingletonVector(byte value) {

        return LinearAlgebra.DEFAULT_FACTORY.createVector(new byte[] {value});
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
     * @return a random vector source
     */
    public static VectorSource asRandomSource(int length) {

        return new RandomVectorSource(length);
    }

    /**
     * Creates a sum vector accumulator that calculates the sum of all elements in the vector.
     * 
     * @param neutral
     *            the neutral value
     * @return a sum accumulator
     */
    public static VectorAccumulator asSumAccumulator(byte neutral) {

        return new SumVectorAccumulator(neutral);
    }

    /**
     * Creates a product vector accumulator that calculates the product of all elements in the vector.
     * 
     * @param neutral
     *            the neutral value
     * @return a product accumulator
     */
    public static VectorAccumulator asProductAccumulator(byte neutral) {

        return new ProductVectorAccumulator(neutral);
    }

    /**
     * Makes a minimum vector accumulator that accumulates the minimum across vector elements.
     * 
     * @return a minimum vector accumulator
     */
    public static VectorAccumulator mkMinAccumulator() {

        return new MinVectorAccumulator();
    }

    /**
     * Makes a maximum vector accumulator that accumulates the maximum across vector elements.
     * 
     * @return a maximum vector accumulator
     */
    public static VectorAccumulator mkMaxAccumulator() {

        return new MaxVectorAccumulator();
    }

    /**
     * Creates a sum function accumulator, that calculates the sum of all
     * elements in the vector after applying given {@code function} to each of them.
     * 
     * @param neutral
     *            the neutral value
     * @param function
     *            the vector function
     * @return a sum function accumulator
     */
    public static VectorAccumulator asSumFunctionAccumulator(byte neutral, VectorFunction function) {

        return new FunctionVectorAccumulator(new SumVectorAccumulator(neutral), function);
    }

    /**
     * Creates a product function accumulator, that calculates the product of
     * all elements in the vector after applying given {@code function} to
     * each of them.
     * 
     * @param neutral
     *            the neutral value
     * @param function
     *            the vector function
     * @return a product function accumulator
     */
    public static VectorAccumulator asProductFunctionAccumulator(byte neutral, VectorFunction function) {

        return new FunctionVectorAccumulator(new ProductVectorAccumulator(neutral), function);
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

        final PrintableAppendable output = new PrintableAppendable(appendable);
        final int N = vector.length();

        // this prints two lines, the first with indexes and the second with the actual values
        // (this only works fine for indices less than 100)
        try {
            for (int j = 0; j < N; j++)
                output.printf("* %02d ", j);

            output.println('|');

            for (int j = 0; j < N; j++)
                output.printf("| %02x ", vector.get(j));
            output.println('|');
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
