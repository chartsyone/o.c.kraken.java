/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import one.chartsy.Series;
import one.chartsy.TimeFrame;

/**
 * The dense (in-memory) implementation of the {@link Series} interface.
 * 
 * 
 * @author Mariusz Bernacki
 *
 */
public class DenseSeries implements Series {

    /** The current time frame. */
    private final TimeFrame timeFrame;
    /** The array for internal storage of elements. */
    private final double[] data;

    /**
     * Construct an empty vector with the specified initial capacity.
     * 
     * @param timeFrame
     *            the time frame of the data storage
     */
    public DenseSeries(TimeFrame timeFrame) {
        this(timeFrame, new double[0]);
    }

    /**
     * Construct an empty vector with the specified initial capacity.
     * 
     * @param timeFrame
     *            the time frame of the data storage
     * @param size
     *            the initial capacity of the vector
     */
    public DenseSeries(TimeFrame timeFrame, int size) {
        this.timeFrame = timeFrame;
        this.data = new double[size];
    }

    /**
     * Construct a constant vector with the specified length.
     * 
     * @param timeFrame
     *            the time frame of the data storage
     * @param size
     *            Length of vector.
     * @param elem
     *            Fill the vector with this scalar value.
     */
    public DenseSeries(TimeFrame timeFrame, int size, double elem) {
        this.timeFrame = timeFrame;
        this.data = new double[size];
        if (elem != 0.0)
            for (int i = 0; i < size; i++)
                data[i] = elem;

    }

    /**
     * Construct a vector from a 1-D array.
     * 
     * @param timeFrame
     *            the time frame of the data storage
     * @param data
     *            one-dimensional array of doubles.
     * @see #constructWithCopy
     */
    public DenseSeries(TimeFrame timeFrame, double[] data) {
        this.timeFrame = timeFrame;
        this.data = data;
    }


    /**
     * Makes a deep copy of a vector
     */
    @Override
    public DenseSeries copy() {
        return new DenseSeries(timeFrame, (double[]) data.clone());
    }

    /**
     * Access the internal one-dimensional array.
     * 
     * @return the pointer to the one-dimensional array of series elements
     */
    public double[] getArray() {
        return data;
    }

    /**
     * Copy the internal one-dimensional array.
     * 
     * @return one-dimensional array copy of series element
     */
    public double[] getArrayCopy() {
        return (double[]) data.clone();
    }

    @Override
    public final TimeFrame getTimeFrame() {
        return timeFrame;
    }

    @Override
    public final double get(int i) {
        return data[i];
    }

    @Override
    public boolean isUndefined(int i) {
        return i < 0 || i >= data.length;
    }

    @Override
    public Series ref(int periods) {
        if (periods > 0)
            throw new IllegalArgumentException("The `periods` argument cannot be positive");
        if (periods == 0)
            return this;
        int newLength = length() + periods;
        if (newLength <= 0)
            return Series.empty(getTimeFrame());

        return new DenseSeries(getTimeFrame(), Arrays.copyOfRange(data, -periods, data.length));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int j = length();
        if (j > 0) {
            double[] a = data;
            sb.append(a[0]);
            for (int i = 1; i < j; i++)
                sb.append(", ").append(a[i]);
        }
        return sb.append(']').toString();
    }

    @Override
    public final int length() {
        return data.length;
    }

    @Override
    public Series add(double y) {
        double[] z = new double[length()];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) + y;

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series add(Series y) {
        TimeFrame.requireSame(this, y);

        double[] z = new double[Math.min(length(), y.length())];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) + y.get(i);
        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series sub(double y) {
        double[] z = new double[length()];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) - y;

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series sub(Series y) {
        TimeFrame.requireSame(this, y);

        double[] z = new double[Math.min(length(), y.length())];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) - y.get(i);
        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series mul(double y) {
        double[] z = new double[length()];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) * y;

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series mul(Series y) {
        TimeFrame.requireSame(this, y);

        double[] z = new double[Math.min(length(), y.length())];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) * y.get(i);
        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series div(double y) {
        double[] z = new double[length()];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) / y;

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series div(Series y) {
        TimeFrame.requireSame(this, y);

        double[] z = new double[Math.min(length(), y.length())];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i) / y.get(i);
        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series map(DoubleUnaryOperator operator) {
        double[] x = this.data;
        double[] z = new double[x.length];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = operator.applyAsDouble(x[i]);

        return new DenseSeries(timeFrame, z);
    }

    public Series mapThread(DoubleBinaryOperator f, double rightValue) {
        double[] z = new double[length()];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = f.applyAsDouble(get(i), rightValue);

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public double[] toArray(int beginBar, int endBar) {
        double[] array = new double[endBar - beginBar];
        System.arraycopy(this.data, beginBar, array, 0, endBar - beginBar);
        return array;
    }
}
