/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

import one.chartsy.data.DenseSeries;


/**
 * Represents a time series with numeric values at times specified by the
 * associated {@link #getTimeFrame() TimeFrame} object.
 * <p>
 * The number of values in a time series is specified by the {@link #length()}.
 * Each series values are indexed from {@code 0} to {@code length() - 1}, and
 * can be accessed using the {@link #get(int)} method.<br>
 * The value at the first time is at index {@code length() - 1}, and can be also
 * obtained using the {@link #getFirst()} method.<br>
 * The value at the last time is at index {@code 0}, and can be also obtained
 * using the {@link #getLast()} method.<br>
 * The {@code Series} interface also contains a number of convenience methods
 * for calculating common statistical and technical indicators, such as moving
 * averages.
 * 
 * 
 * @author Mariusz Bernacki
 *
 */
public interface Series {

    /**
     * Construct an empty vector with the specified initial capacity.
     * 
     * @param timeFrame
     *            the time frame of the data storage
     * @param size
     *            the initial capacity of the vector
     */
    public static Series of(TimeFrame timeFrame, int size) {
        return new DenseSeries(timeFrame, size);
    }

    /**
     * Constructs a new dense series from the given array of values.
     * 
     * <p>
     * The resulting series indexing order matches the {@code values} array
     * indexing order. The {@code values} array is not copied upon series
     * creation thus it <b>cannot</b> be modified by a caller.
     * 
     * @param timeFrame
     *            the time frame associated with the series
     * @param values
     *            the array of series values
     */
    public static Series of(TimeFrame timeFrame, double[] values) {
        return new DenseSeries(timeFrame, values);
    }

    /**
     * Constructs a new {@code Series} object, using the provided generator
     * function to compute each element.
     * 
     * <p>
     * The result of {@code generator.applyAsDouble(index)} method call matches
     * the value of {@code result.get(index)} of the returned {@code result}. If
     * the generator function throws an exception, it is relayed to the caller.
     *
     * @param timeFrame
     *            the time frame of the result
     * @param length
     *            the target length of the result
     * @param generator
     *            a function accepting an index and producing the desired value
     *            for that position
     * @throws NullPointerException
     *             if the {@code generator} is {@code null}
     * @return the newly created {@code Series}, having the specified
     *         {@code length}, and whose {@code get(index)} value matches the
     *         result of the {@code generator.applyAsDouble(index)}
     */
    public static Series of(TimeFrame timeFrame, int length, IntToDoubleFunction generator) {
        double[] values = new double[length];
        Arrays.setAll(values, generator);
        return Series.of(timeFrame, values);
    }

    /**
     * Makes a deep copy of a vector
     */
    default DenseSeries copy() {
        int size = length();
        double[] result = new double[size];
        for (int i = 0; i < size; i++)
            result[i] = get(i);
        return new DenseSeries(getTimeFrame(), result);
    }

    /**
     * Returns the time frame in which all values of this series are represented
     * (e.g., weekly, daily, hourly...)
     * 
     * <p>
     * By default all methods declared on this object, returning the
     * {@code Series} object, give the {@code Series} result with the exact same
     * time frame.
     * 
     * @return the {@code TimeFrame} of this data series
     */
    TimeFrame getTimeFrame();

    /**
     * Returns the data series value at the specified position.
     * 
     * @param i
     *            the index to get the value at
     * @return the value at index {@code i}
     * @throws IndexOutOfBoundsException
     *             (possibly an instance of
     *             {@code ArrayIndexOutOfBoundsException}) if the index
     *             {@code i} is outside of the range of this series, i.e. either
     *             {@code < 0} or {@code >= this.length()}
     */
    double get(int i);

    /**
     * Gets the data series value at the specified position if it exists, or
     * {@code defaultValue} otherwise.
     * 
     * @param index
     *            the index to get the value at
     * @param defaultValue
     *            the value to be returned if the series value at index
     *            {@code i} doesn't exist
     * @return the series value at the {@code index} or the {@code defaultValue}
     *         if the {@code index} is outside of the series range, i.e. it is
     *         {@code < 0} or {@code >= length()}
     */
    default double get(int index, double defaultValue) {
        if (index < 0 || index >= length())
            return defaultValue;

        return get(index);
    }

    /**
     * Gives the number of values in this series.
     * 
     * @return the non-negative {@code this} series length
     */
    int length();

    /**
     * Applies the binary function to each element of both this and the other
     * series to produce a new time series.
     * <p>
     * The elements from {@code this} series are used as the <i>left</i>
     * arguments of the function {@code f}.<br>
     * The elements from the {@code other} series are used as the <i>right</i>
     * arguments of the function {@code f}.<br>
     * The given operator {@code f} is applied to each series value one by one,
     * starting from the farthest possible element to the closest one at
     * {@code index = 0}. The resulting series have length equal to the length
     * of the shortest of {@code this} and the {@code other} series. Both series
     * must have the same {@link #getTimeFrame() time frame}, otherwise an
     * {@code IllegalArgumentException} is thrown.
     * 
     * @param f
     *            the real-valued mapping function to use
     * @param other
     *            the other series whose elements are to be applied to the
     *            function {@code f}
     * @return the new time series
     */
    default Series mapThread(DoubleBinaryOperator f, Series other) {
        TimeFrame.requireSame(this, other);

        double[] r = new double[Math.min(length(), other.length())];
        for (int i = r.length-1; i >= 0; i--)
            r[i] = f.applyAsDouble(get(i), other.get(i));
        return Series.of(getTimeFrame(), r);
    }

    /**
     * Applies the binary function to each element in the series to produce a
     * new time series, using specified constant as the function right argument.
     * <p>
     * The current series value is used as the <b>left</b> argument of the
     * function {@code f}.<br>
     * The given operator {@code f} is applied to each series value one by one,
     * starting from the farthest element at index {@code length - 1} to the
     * closest at {@code index = 0}. The resulting series have the same
     * {@link #length()} and {@link #getTimeFrame() timeFrame} as contained in
     * {@code this}.
     * 
     * @param f
     *            the real-valued mapping function to use
     * @param rightValue
     *            the constant right argument to be applied to the function
     *            {@code f}
     * @return the new time series
     */
    default Series mapThread(DoubleBinaryOperator f, double rightValue) {
        double[] z = new double[length()];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = f.applyAsDouble(get(i), rightValue);

        return Series.of(getTimeFrame(), z);
    }

    /**
     * Applies the binary function to each element in the series to produce a
     * new time series, using specified constant as the function left argument.
     * <p>
     * The current series value is used as the <b>right</b> argument of the
     * function {@code f}.<br>
     * The given operator {@code f} is applied to each series value one by one,
     * starting from the farthest element at index {@code length - 1} to the
     * closest at {@code index = 0}. The resulting series have the same
     * {@link #length()} and {@link #getTimeFrame() timeFrame} as contained in
     * {@code this}.
     * 
     * @param leftValue
     *            the constant left argument to be applied to the function
     *            {@code f}
     * @param f
     *            the real-valued mapping function to use
     * @return the new time series
     */
    default Series mapThread(double leftValue, DoubleBinaryOperator f) {
        double[] z = new double[length()];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = f.applyAsDouble(leftValue, get(i));

        return Series.of(getTimeFrame(), z);
    }

    /**
     * Addition.
     * 
     * @param y
     * @return
     */
    default Series add(Series y) {
        return mapThread(Double::sum, y);
    }

    /**
     * Adds a constant to the underlying series.
     * 
     * @param y
     *            the constant to be added to each series value
     * @return the series of length {@code this.length()} with the specified
     *         constant added
     */
    default Series add(double y) {
        return mapThread(Double::sum, y);
    }

    /**
     * Multiplies the series by the other one.
     * 
     * @param other
     *            the other series to be multiplied by
     * @return the new time series representing product of {@code this} and
     *         {@code other} series
     */
    default Series mul(Series y) {
        return mapThread(DoubleMath::multiply, y);
    }

    /**
     * Multiplies the series by the specified constant value.
     * 
     * @param y
     *            the value to be multiplied by
     * @return the new time series representing product of {@code this} series
     *         and {@code y}
     */
    default Series mul(double y) {
        return mapThread(DoubleMath::multiply, y);
    }

    /**
     * Applies the function to each element in the series to produce a new time
     * series.
     * <p>
     * The given operator {@code f} is applied to each series value one by one,
     * starting from the farthest element at index {@code length - 1} to the
     * closest at {@code index = 0}. The resulting series have the same
     * {@link #length()} and {@link #getTimeFrame() timeFrame} as contained in
     * {@code this}.
     * 
     * @param f
     *            the real-valued mapping function to use
     * @return the new time series
     */
    default Series map(DoubleUnaryOperator f) {
        double[] z = new double[length()];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = f.applyAsDouble(get(i));

        return Series.of(getTimeFrame(), z);
    }

    /**
     * Gives the first (chronologically oldest) element in {@code this}.
     * 
     * @return the element at position {@code length()-1}
     * @throws IndexOutOfBoundsException
     *             if this data series is empty
     */
    default double getFirst() {
        return get(length() - 1);
    }

    /**
     * Gives the last (chronologically newest) element in {@code this}.
     * 
     * @return the element at position {@code 0}
     * @throws IndexOutOfBoundsException
     *             if this data series is empty
     */
    default double getLast() {
        return get(0);
    }

    /**
     * Performs an element-wise division of {@code this} and {@code y} series.
     * 
     * @param y
     *            the divisors
     * @return the sequence of quotients, of length
     *         {@code min(this.length(), y.length())}
     * @throws IllegalArgumentException
     *             if {@code this} and {@code y} series differ in the time frame
     */
    default Series div(Series y) {
        return mapThread(DoubleMath::divide, y);
    }

    /**
     * Performs an element-wise division of {@code this} and {@code y} value.
     * 
     * @param y
     *            the divisor
     * @return the sequence of quotients, of length {@code this.length()}
     */
    default Series div(double y) {
        return mapThread(DoubleMath::divide, y);
    }

    /**
     * Performs an element-wise division of {@code x} and {@code y} values.
     * 
     * @param x
     *            the dividend
     * @param y
     *            the divisors
     * @return the sequence of quotients, of length {@code y.length()}
     */
    public static Series div(double x, Series y) {
        return y.mapThread(x, DoubleMath::divide);
    }

    /**
     * Subtraction.
     * 
     * @param y
     * @return
     */
    default Series sub(Series y) {
        return mapThread(DoubleMath::subtract, y);
    }

    default Series sub(double y) {
        return mapThread(DoubleMath::subtract, y);
    }

    public static Series sub(double x, Series y) {
        return y.mapThread(x, DoubleMath::subtract);
    }

    default Series max(Series y) {
        return this.mapThread(Double::max, y);
    }

    default Series min(Series y) {
        return mapThread(Double::min, y);
    }

    Series ref(int periods);

    /**
     * Gives the simple moving average of the series, computed by taking the
     * arithmetic mean over the specified {@code periods}.
     * <p>
     * The simple moving average is commonly abbreviated as SMA.<br>
     * The SMA is a statistical indicator.<br>
     * For example the calculation of {@code sma(2)} over the series {3, 9, 9,
     * -8, 6} gives {6, 9, 0.5, -1}. As a special corner case, the
     * {@code sma(1)} gives the result matching the original series.
     * 
     * @param periods
     *            the moving average period
     * @return the series of arithmetic means, of length
     *         {@code this.length()-periods+1}
     * @throws IllegalArgumentException
     *             if the {@code periods} argument is not positive
     */
    default Series sma(int periods) {
        if (periods <= 0)
            throw new IllegalArgumentException("The `periods` argument " + periods + " must be positive integer");
        int newLength = length() - periods + 1;
        if (newLength <= 0)
            return empty(getTimeFrame());

        double[] result = new double[newLength];
        double value = 0.0;
        int i = length();
        for (int k = 0; k < periods; k++)
            value += get(--i);

        double coeff = 1.0/periods;
        result[i] = value *= coeff;
        while (--i >= 0)
            result[i] = value += ((get(i) - get(i + periods))*coeff);
        return Series.of(getTimeFrame(), result);
    }

    default boolean isUndefined(int i) {
        return i < 0 || i >= length();
    }

    /**
     * Gives the double exponential moving average of the series.
     * <p>
     * The double exponential moving average is commonly abbreviated as
     * DEMA.<br>
     * The DEMA is a moving average overlay which can be used as a direct
     * replacement for other moving averages like {@link #ema(int) EMA} or
     * {@link #sma(int) SMA}. The DEMA, similarly like EMA, is more responsive
     * to price fluctuations than a simple moving average.
     * <p>
     * The method is equivalent to, for the series {@code x}:
     * 
     * <pre>
     * {@code x.ema(periods).mul(2).sub(x.ema(periods).ema(periods))}
     * </pre>
     * 
     * @param periods
     *            the number of look back bars considered for each exponential
     *            moving average calculation
     * @return the time series of length {@code n - 2*periods + 2}, where
     *         {@code n} is the length of {@code this} series
     * @see #tema(int)
     * @see #ema(int)
     */
    default Series dema(int periods) {
        Series ema1 = this.ema(periods);
        Series ema2 = ema1.ema(periods);

        return ema1.mapThread((v, w) -> 2*v - w, ema2);
    }

    /**
     * Gives the triple exponential moving average of the series.
     * <p>
     * The triple exponential moving average is commonly abbreviated as
     * TEMA.<br>
     * The TEMA was developed by Patrick Mulloy.<br>
     * The TEMA is a moving average overlay which can be used as a direct
     * replacement for other moving averages like {@link #ema(int) EMA} or
     * {@link #sma(int) SMA}.
     * <p>
     * The method is equivalent to, for the series {@code x}:
     * 
     * <pre>
     * {@code x.ema(periods).mul(3)
     *  .sub(x.ema(periods).ema(periods).mul(3))
     *  .add(x.ema(periods).ema(periods).ema(periods))}
     * </pre>
     * 
     * @param periods
     *            the number of look back bars considered for each exponential
     *            moving average calculation
     * @return the time series of length {@code n - 3*periods + 3}, where
     *         {@code n} is the length of {@code this} series
     * @see #dema(int)
     * @see #ema(int)
     */
    default Series tema(int periods) {
        Series ema1 = this.ema(periods);
        Series ema2 = ema1.ema(periods);

        return ema1.mapThread((v, w) -> 3*v - 3*w, ema2).add(ema2.ema(periods));
    }

    /**
     * Gives the triangular moving average of the series.
     * <p>
     * The triangular moving average is sometimes abbreviated as TMA.<br>
     * The TMA is a double-smoothed simple moving average. The method is
     * equivalent to, for the series {@code x}:
     * 
     * <pre>
     * {@code x.sma(periods).sma(periods)}
     * </pre>
     * 
     * @param periods
     *            the number of bars consisting the indicator averaging period
     * @return the time series of length {@code n - periods + 1}, where
     *         {@code n} is the length of {@code this} series
     */
    default Series tma(int periods) {
        return sma(periods).sma(periods);
    }

    default Series ema(int periods) {
        if (periods <= 0)
            throw new IllegalArgumentException("The periods argument must be positive; but was " + periods);
        int newLength = length() - periods + 1;
        if (newLength <= 0)
            return empty(getTimeFrame());

        double[] z = new double[newLength];
        double value = 0.0;
        int i = length();
        for (int k = 0; k < periods; k++)
            value += get(--i);

        z[i] = value /= periods;
        double alpha = 2.0/(periods + 1);
        while (--i >= 0)
            z[i] = value += (get(i) - value)*alpha;
        return Series.of(getTimeFrame(), z);
    }

    /**
     * Computes the <i>Wilders Moving Average</i> of the price series.
     * <p>
     * The Wilders MA was developed by Welles Wilder.<br>
     * The Wilders MA is a variation of an exponential moving average and is
     * used for computation other indicators developed by the same author, such
     * as {@link Quotes#rsi(int) RSI}, {@link Quotes#atr(int) ATR} and
     * {@link Quotes#adx(int) ADX}.
     * 
     * @param periods
     *            the moving average smoothing parameter
     * @return the Wilders moving average series of length {@code n-periods+1},
     *         where {@code n} is the length of {@code this} series
     * @throws IllegalArgumentException
     *             if the {@code periods} parameter is not positive
     */
    default Series wilders(int periods) {
        if (periods <= 0)
            throw new IllegalArgumentException("The `periods` argument " + periods + " must be a positive integer");
        int newLength = length() - periods + 1;
        if (newLength <= 0)
            return empty(getTimeFrame());

        double[] result = new double[newLength];
        double value = 0.0;
        int i = length();
        for (int k = 0; k < periods; k++)
            value += get(--i);

        double alpha = 1.0 / periods;
        result[i] = value *= alpha;
        while (--i >= 0)
            result[i] = value += (get(i) - value)*alpha;
        return Series.of(getTimeFrame(), result);
    }

    /**
     * Computes the successive differences of values in {@code this} series.
     * 
     * @return the sequence of successive differences, of length
     *         {@code this.length()-1}
     * @see #differences(int)
     */
    default Series differences() {
        int i = length() - 1;
        if (i <= 0)
            return empty(getTimeFrame());

        double[] z = new double[i];
        double curr;
        for (double prev = get(i); --i >= 0; prev = curr)
            z[i] = (curr = get(i)) - prev;
        return Series.of(getTimeFrame(), z);
    }

    default Series rsi(int periods) {
        double upVal = 0;
        double dnVal = 0;
        Series diff = differences();
        int length = diff.length();
        if (length-periods <= 0)
            return empty(getTimeFrame());

        double[] z = new double[length-periods];
        for (int i = 1; i <= periods; i++) {
            double val = diff.get(length - i);
            if (val > 0)
                upVal += val;
            else
                dnVal -= val;
        }
        upVal /= periods;
        dnVal /= periods;
        double prev = 50.0;
        for (int i = length-periods-1; i >= 0; i--) {
            double val = diff.get(i);
            double pVal = 0, nVal = 0;
            if (val > 0)
                pVal = val;
            else
                nVal = -val;
            upVal = (upVal * (periods - 1) + pVal) / periods;
            dnVal = (dnVal * (periods - 1) + nVal) / periods;
            if (upVal + dnVal != 0)
                z[i] = prev = (100.0 * upVal) / (upVal + dnVal);
            else
                z[i] = prev;
        }
        return Series.of(getTimeFrame(), z);
    }

    default double[] toArray() {
        return toArray(0, length());
    }

    default double[] toArray(int start, int end) {
        double[] array = new double[end - start];
        for (int i = 0; i < array.length; i++)
            array[i] = get(i + start);
        return array;
    }

    public static Series empty(TimeFrame timeFrame) {
        return new DenseSeries(timeFrame);
    }
}

/**
 * Static helper methods for common double-arithmetic operations, serving a
 * {@code BinaryOperator} handlers.
 * <p>
 * This helper class does not provide addition method, use {@code Double::sum}
 * instead.
 * 
 * @author Mariusz Bernacki
 */
final class DoubleMath {

    static double subtract(double x, double y) {
        return x - y;
    }

    static double multiply(double x, double y) {
        return x * y;
    }

    static double divide(double x, double y) {
        return x / y;
    }
}