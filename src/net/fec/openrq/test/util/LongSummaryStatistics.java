/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package net.fec.openrq.test.util;

/**
 * A state object for collecting statistics such as count, min, max, sum, and average. This implementation does not
 * check for overflow of the sum.
 * <p>
 * NOTE: <b>This class was adapted from class {@code java.util.LongSummaryStatistics} in JDK8.</b>
 */
public class LongSummaryStatistics {

    private long count;
    private long sum;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;


    /**
     * Construct an empty instance with zero count, zero sum, {@code Long.MAX_VALUE} min, {@code Long.MIN_VALUE} max and
     * zero
     * average.
     */
    public LongSummaryStatistics() {

    }

    /**
     * Records a new {@code int} value into the summary information.
     * 
     * @param value
     *            the input value
     */
    public void accept(int value) {

        accept((long)value);
    }

    /**
     * Records a new {@code long} value into the summary information.
     * 
     * @param value
     *            the input value
     */
    public void accept(long value) {

        ++count;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * Combines the state of another {@code LongSummaryStatistics} into this
     * one.
     * 
     * @param other
     *            another {@code LongSummaryStatistics}
     * @throws NullPointerException
     *             if {@code other} is null
     */
    public void combine(LongSummaryStatistics other) {

        count += other.count;
        sum += other.sum;
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
    }

    /**
     * Returns the count of values recorded.
     * 
     * @return the count of values
     */
    public final long getCount() {

        return count;
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     * 
     * @return the sum of values, or zero if none
     */
    public final long getSum() {

        return sum;
    }

    /**
     * Returns the minimum value recorded, or {@code Long.MAX_VALUE} if no
     * values have been recorded.
     * 
     * @return the minimum value, or {@code Long.MAX_VALUE} if none
     */
    public final long getMin() {

        return min;
    }

    /**
     * Returns the maximum value recorded, or {@code Long.MIN_VALUE} if no
     * values have been recorded
     * 
     * @return the maximum value, or {@code Long.MIN_VALUE} if none
     */
    public final long getMax() {

        return max;
    }

    /**
     * Returns the arithmetic mean of values recorded, or zero if no values have been
     * recorded.
     * 
     * @return The arithmetic mean of values, or zero if none
     */
    public final double getAverage() {

        return getCount() > 0 ? (double)getSum() / getCount() : 0.0d;
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    public String toString() {

        return String.format(
            "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
            this.getClass().getSimpleName(),
            getCount(),
            getSum(),
            getMin(),
            getAverage(),
            getMax());
    }
}
