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
 * Contributor(s): Daniel Renshaw
 * Ewald Grusk
 * Jakob Moellers
 * Maxim Samoylov
 * Miron Aseev
 */
package net.fec.openrq.util.linearalgebra.vector;


import static net.fec.openrq.util.arithmetic.OctetOps.aDividedByB;
import static net.fec.openrq.util.arithmetic.OctetOps.aIsEqualToB;
import static net.fec.openrq.util.arithmetic.OctetOps.aMinusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aPlusB;
import static net.fec.openrq.util.arithmetic.OctetOps.aTimesB;
import static net.fec.openrq.util.linearalgebra.vector.ByteVectors.printVector;

import java.util.Random;

import net.fec.openrq.util.linearalgebra.factory.Factory;
import net.fec.openrq.util.linearalgebra.matrix.ByteMatrix;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorAccumulator;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorFunction;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorPredicate;
import net.fec.openrq.util.linearalgebra.vector.functor.VectorProcedure;


public abstract class AbstractByteVector implements ByteVector {

    protected int length;

    protected Factory factory;


    protected AbstractByteVector(Factory factory, int length) {

        ensureLengthIsCorrect(length);

        this.factory = factory;
        this.length = length;
    }

    @Override
    public void swap(int i, int j) {

        if (i != j) {
            byte s = get(i);
            set(i, get(j));
            set(j, s);
        }
    }

    @Override
    public void assign(byte value) {

        update(ByteVectors.asConstFunction(value));
    }

    @Override
    public int length() {

        return length;
    }

    @Override
    public ByteVector add(byte value) {

        return add(value, factory);
    }

    @Override
    public ByteVector add(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aPlusB(get(i), value));
        }

        return result;
    }

    @Override
    public ByteVector add(ByteVector vector) {

        return add(vector, factory);
    }

    @Override
    public ByteVector add(ByteVector vector, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(vector, "vector");

        if (length != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + length + ".");
        }

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aPlusB(get(i), vector.get(i)));
        }

        return result;
    }

    @Override
    public ByteVector multiply(byte value) {

        return multiply(value, factory);
    }

    @Override
    public ByteVector multiply(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aTimesB(get(i), value));
        }

        return result;
    }

    @Override
    public ByteVector hadamardProduct(ByteVector vector) {

        return hadamardProduct(vector, factory);
    }

    @Override
    public ByteVector hadamardProduct(ByteVector vector, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(vector, "vector");

        if (length != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + length + ".");
        }

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aTimesB(get(i), vector.get(i)));
        }

        return result;
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix) {

        return multiply(matrix, factory);
    }

    @Override
    public ByteVector multiply(ByteMatrix matrix, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(matrix, "matrix");

        if (length != matrix.rows()) {
            fail("Wrong matrix dimensions: " + matrix.rows() + "x" + matrix.columns() +
                 ". Should be: " + length + "x_.");
        }

        ByteVector result = factory.createVector(matrix.columns());

        for (int j = 0; j < matrix.columns(); j++) {

            byte acc = 0;

            for (int i = 0; i < matrix.rows(); i++) {
                final byte prod = aTimesB(get(i), matrix.get(i, j));
                acc = aPlusB(acc, prod);
            }

            result.set(j, acc);
        }

        return result;
    }

    @Override
    public ByteVector subtract(byte value) {

        return subtract(value, factory);
    }

    @Override
    public ByteVector subtract(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aMinusB(get(i), value));
        }

        return result;
    }

    @Override
    public ByteVector subtract(ByteVector vector) {

        return subtract(vector, factory);
    }

    @Override
    public ByteVector subtract(ByteVector vector, Factory factory) {

        ensureFactoryIsNotNull(factory);
        ensureArgumentIsNotNull(vector, "vector");

        if (length != vector.length()) {
            fail("Wrong vector length: " + vector.length() + ". Should be: " + length + ".");
        }

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aMinusB(get(i), vector.get(i)));
        }

        return result;
    }

    @Override
    public ByteVector divide(byte value) {

        return divide(value, factory);
    }

    @Override
    public ByteVector divide(byte value, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, aDividedByB(get(i), value));
        }

        return result;
    }

    @Override
    public byte product() {

        return fold(ByteVectors.asProductAccumulator((byte)1));
    }

    @Override
    public byte sum() {

        return fold(ByteVectors.asSumAccumulator((byte)0));
    }

    @Override
    public ByteVector blank() {

        return blank(factory);
    }

    @Override
    public ByteVector blank(Factory factory) {

        ensureFactoryIsNotNull(factory);

        return factory.createVector(length);
    }

    @Override
    public ByteVector copy() {

        return copy(factory);
    }

    @Override
    public ByteVector copy(Factory factory) {

        ensureFactoryIsNotNull(factory);

        return factory.createVector(this);
    }

    @Override
    public ByteVector resize(int length) {

        return resize(length, factory);
    }

    @Override
    public ByteVector resize(int length, Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = factory.createVector(length);

        for (int i = 0; i < Math.min(length, this.length); i++) {
            result.set(i, get(i));
        }

        return result;
    }

    @Override
    public ByteVector shuffle() {

        return shuffle(factory);
    }

    @Override
    public ByteVector shuffle(Factory factory) {

        ensureFactoryIsNotNull(factory);

        ByteVector result = copy(factory);

        // Conduct Fisher-Yates shuffle
        Random rnd = new Random();

        for (int i = 0; i < length; i++) {
            int ii = rnd.nextInt(length - i) + i;

            byte a = result.get(ii);
            result.set(ii, result.get(i));
            result.set(i, a);
        }

        return result;
    }

    @Override
    public ByteVector sliceLeft(int until) {

        return slice(0, until, factory);
    }

    @Override
    public ByteVector sliceLeft(int until, Factory factory) {

        return slice(0, until, factory);
    }

    @Override
    public ByteVector sliceRight(int from) {

        return slice(from, length, factory);
    }

    @Override
    public ByteVector sliceRight(int from, Factory factory) {

        return slice(from, length, factory);
    }

    @Override
    public ByteVector slice(int from, int until) {

        return slice(from, until, factory);
    }

    @Override
    public ByteVector slice(int from, int until, Factory factory) {

        ensureFactoryIsNotNull(factory);

        if (until - from < 0) {
            fail("Wrong slice range: [" + from + ".." + until + "].");
        }

        ByteVector result = factory.createVector(until - from);

        for (int i = from; i < until; i++) {
            result.set(i - from, get(i));
        }

        return result;
    }

    @Override
    public ByteVector select(int[] indices) {

        return select(indices, factory);
    }

    @Override
    public ByteVector select(int[] indices, Factory factory) {

        int newLength = indices.length;

        if (newLength == 0) {
            fail("No elements selected.");
        }

        ByteVector result = factory.createVector(newLength);

        for (int i = 0; i < newLength; i++) {
            result.set(i, get(indices[i]));
        }

        return result;
    }

    @Override
    public Factory factory() {

        return factory;
    }

    @Override
    public void each(VectorProcedure procedure) {

        for (int i = 0; i < length; i++) {
            procedure.apply(i, get(i));
        }
    }

    @Override
    public void eachNonZero(VectorProcedure procedure) {

        for (int i = 0; i < length; i++) {
            if (!aIsEqualToB(get(i), (byte)0)) {
                procedure.apply(i, get(i));
            }
        }
    }

    @Override
    public byte max() {

        return fold(ByteVectors.mkMaxAccumulator());
    }

    @Override
    public byte min() {

        return fold(ByteVectors.mkMinAccumulator());
    }

    @Override
    public ByteVector transform(VectorFunction function) {

        return transform(function, factory);
    }

    @Override
    public ByteVector transform(VectorFunction function, Factory factory) {

        ByteVector result = blank(factory);

        for (int i = 0; i < length; i++) {
            result.set(i, function.evaluate(i, get(i)));
        }

        return result;
    }

    @Override
    public ByteVector transform(int i, VectorFunction function) {

        return transform(i, function, factory);
    }

    @Override
    public ByteVector transform(int i, VectorFunction function, Factory factory) {

        ByteVector result = copy(factory);
        result.set(i, function.evaluate(i, get(i)));

        return result;
    }

    @Override
    public void update(VectorFunction function) {

        for (int i = 0; i < length; i++) {
            set(i, function.evaluate(i, get(i)));
        }
    }

    @Override
    public void updateNonZeros(VectorFunction function) {

        for (int i = 0; i < length; i++) {
            final byte val = get(i);
            if (!aIsEqualToB(val, (byte)0)) {
                set(i, function.evaluate(i, val));
            }
        }
    }

    @Override
    public void update(int i, VectorFunction function) {

        set(i, function.evaluate(i, get(i)));
    }

    @Override
    public byte fold(VectorAccumulator accumulator) {

        for (int i = 0; i < length; i++) {
            accumulator.update(i, get(i));
        }

        return accumulator.accumulate();
    }

    @Override
    public boolean is(VectorPredicate predicate) {

        boolean result = true;

        for (int i = 0; i < length; i++) {
            result = result && predicate.test(i, get(i));
        }

        return result;
    }

    @Override
    public boolean non(VectorPredicate predicate) {

        return !is(predicate);
    }

    @Override
    public ByteMatrix toRowMatrix() {

        return toRowMatrix(factory);
    }

    @Override
    public ByteMatrix toRowMatrix(Factory factory) {

        ByteMatrix result = factory.createMatrix(1, length);
        result.setRow(0, this);
        return result;
    }

    @Override
    public ByteMatrix toColumnMatrix() {

        return toColumnMatrix(factory);
    }

    @Override
    public ByteMatrix toColumnMatrix(Factory factory) {

        ByteMatrix result = factory.createMatrix(length, 1);
        result.setColumn(0, this);
        return result;
    }

    @Override
    public int hashCode() {

        int result = 17;

        for (int i = 0; i < length; i++) {
            long value = get(i);
            result = 37 * result + (int)(value ^ (value >>> 32));
        }

        return result;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        // instanceof also checks for null
        if (!(object instanceof ByteVector)) {
            return false;
        }

        ByteVector vector = (ByteVector)object;

        if (length != vector.length()) {
            return false;
        }

        boolean result = true;

        for (int i = 0; result && i < length; i++) {
            result = aIsEqualToB(get(i), vector.get(i));
        }

        return result;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        printVector(this, sb);
        return sb.toString();
    }

    protected void ensureFactoryIsNotNull(Factory factory) {

        ensureArgumentIsNotNull(factory, "factory");
    }

    protected void ensureLengthIsCorrect(int length) {

        if (length < 0) {
            fail("Wrong vector length: " + length);
        }
        if (length == Integer.MAX_VALUE) {
            fail("Wrong vector length: use 'Integer.MAX_VALUE - 1' instead.");
        }
    }

    protected void ensureArgumentIsNotNull(Object argument, String name) {

        if (argument == null) {
            fail("Bad argument: \"" + name + "\" is 'null'.");
        }
    }

    protected void fail(String message) {

        throw new IllegalArgumentException(message);
    }
}
