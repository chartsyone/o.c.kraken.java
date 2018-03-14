/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToDoubleFunction;

import one.chartsy.data.DenseQuotes;
import one.chartsy.data.DenseSeries;
import one.chartsy.time.Chronological;

/**
 * Represents an ordered sequence of instrument prices.
 * <p>
 * The {@code Quotes} are described by an instrument {@link #getSymbol()
 * Symbol}, data {@link #getTimeFrame() TimeFrame} and a sequence of prices
 * accessible using the {@link #get(int)} method. Essentially the data is an
 * array of {@link Quote}'s where the index {@code 0} is the last (latest, most
 * recent) bar and the index {@code length()-1} is the first (chronologically
 * oldest) bar. The {@code Quotes} interface also contains a number of
 * convenience methods for calculating common statistical and technical
 * indicators, such as moving averages.
 * 
 * @author Mariusz Bernacki
 *
 */
public interface Quotes {

    /**
     * Constructs a new {@code Quotes} instance from the specified list of bars. The
     * {@code list} must be given in chronological order, i.e. with the oldest bar
     * given first in the supplied collection.
     * 
     * @param symbol
     *            the symbol
     * @param timeFrame
     *            the time frame
     * @param list
     *            the sequence of bars in chronological order, from the oldest
     * @return the {@code Quotes} object of length {@code list.size()}
     */
    public static Quotes of(Symbol symbol, TimeFrame timeFrame, Collection<Quote> list) {
        int k = list.size();
        Quote[] quotes = new Quote[k];
        for (Quote quote : list)
            quotes[--k] = quote;

        return new DenseQuotes(symbol, timeFrame, quotes);
    }

    /**
     * Constructs a new {@code Quotes} instance from the specified bars sequence.
     * The {@code Iterator} must produce bars in chronological order, i.e. with the
     * oldest bar given first in the supplied bar sequence.
     * 
     * @param symbol
     *            the symbol
     * @param timeFrame
     *            the time frame
     * @param iter
     *            the sequence of bars in chronological order, from the oldest
     * @return the {@code Quotes} object of length {@code stream.count()}
     */
    public static Quotes of(Symbol symbol, TimeFrame timeFrame, Iterator<Quote> iter) {
        List<Quote> list = new ArrayList<>(256);
        iter.forEachRemaining(list::add);
        return of(symbol, timeFrame, list);
    }

    default Quote[] toArray() {
        Quote[] array = new Quote[length()];
        Arrays.setAll(array, this::get);
        return array;
    }

    /**
     * Trims the length of this {@code Quotes} instance to be at most the specified
     * new length. The result will contain the most recent bars, while the oldest
     * bars will be dropped from the result. An application can use this operation
     * to minimize the storage of a {@code Quotes} instance. If the current length
     * is already less than or equal to the specified target length, the method
     * simply returns {@code this} instance.
     * 
     * @param newLength
     *            the target dataset length in bars
     * @return the {@code Quotes} with target length or shorter
     */
    Quotes trimToLength(int newLength);

    /**
     * Returns the data series value at the specified position.
     * 
     * @param barNo
     *            the index to get the value at
     * @return the quote object at index {@code barNo}
     * @throws IndexOutOfBoundsException
     *             (possibly an instance of
     *             {@code ArrayIndexOutOfBoundsException}) if the index
     *             {@code barNo} is outside of the range of this series, i.e. either
     *             {@code < 0} or {@code >= this.length()}
     */
    Quote get(int barNo) throws IndexOutOfBoundsException;

    /**
     * Gives the timestamp of the bar at the specified {@code barNo} position.
     * The method is equivalent to:
     * <pre>{@code this.get(barNo).time}</pre>
     * 
     * @param barNo
     *            the index to get the time value at
     * @return the time of the quote at the {@code barNo} position
     * @throws IndexOutOfBoundsException
     *             (possibly an instance of
     *             {@code ArrayIndexOutOfBoundsException}) if the index
     *             {@code barNo} is outside of the range of this series, i.e. either
     *             {@code < 0} or {@code >= this.length()}
     */
    default long timeAt(int barNo) throws IndexOutOfBoundsException {
        return get(barNo).time;
    }

    /**
     * Gives the first (chronologically oldest) element in {@code this}.
     * 
     * @return the element at position {@code length() - 1}
     * @throws IndexOutOfBoundsException
     *             if this data series is empty
     */
    default Quote getFirst() {
        return get(length() - 1);
    }

    /**
     * Gives the last (chronologically newest) element in {@code this}.
     * 
     * @return the element at position {@code 0}
     * @throws IndexOutOfBoundsException
     *             if this data series is empty
     */
    default Quote getLast() {
        return get(0);
    }

    // TODO: not sure if we really need this 2 shortcut methods, cause it seems to spoil the API
    //	/**
    //	 * Gives the datetime of the first (chronologically oldest) quote in
    //	 * {@code this}.
    //	 * <p>
    //	 * The method is effectively equivalent to, for {@code this} quotes:
    //	 * <pre>{@code this.getFirst().getDateTime()}</pre>
    //	 * 
    //	 * @return the {@code LocalDateTime} of the oldest {@link Quote}
    //	 * @see #getLastDateTime()
    //	 * @see #getFirst()
    //	 * @see Quote#getDateTime()
    //	 */
    //	default LocalDateTime getFirstDateTime() {
    //		return getFirst().getDateTime();
    //	}
    //	
    //	/**
    //	 * Gives the datetime of the last (chronologically newest) quote in
    //	 * {@code this}.
    //	 * <p>
    //	 * The method is effectively equivalent to, for {@code this} quotes:
    //	 * <pre>{@code this.getLast().getDateTime()}</pre>
    //	 * 
    //	 * @return the {@code LocalDateTime} of the newest {@link Quote}
    //	 * @see #getFirstDateTime()
    //	 * @see #getLast()
    //	 * @see Quote#getDateTime()
    //	 */
    //	default LocalDateTime getLastDateTime() {
    //		return getLast().getDateTime();
    //	}

    /**
     * Gives the {@code Symbol} (i.e. instrument) associated with this
     * {@code Quotes}.
     * 
     * @return the instrument symbol for this quotes
     */
    Symbol getSymbol();

    /**
     * Returns the time frame in which all quotes of this series are represented
     * (e.g., weekly, daily, hourly...)
     * 
     * <p>
     * By default all methods declared on this object, returning the
     * {@code Series} object, give the {@code Series} result with the exact same
     * time frame.
     * 
     * @return the {@code TimeFrame} of this data series
     * @see Series#getTimeFrame()
     */
    TimeFrame getTimeFrame();

    /**
     * Gives the number of quotes in this series.
     * 
     * @return the non-negative {@code this} series length
     */
    int length();

    /**
     * Gives the simple moving average of the series, computed by taking the
     * arithmetic mean over the specified {@code periods}.
     * <p>
     * The simple moving average is commonly abbreviated as SMA. See
     * {@link Series#sma(int)} for more details.<br>
     * The method is effectively equivalent to, for {@code this} quotes:
     * <pre>{@code this.closes().sma(periods)}</pre>
     * 
     * @param periods
     *            the moving average period
     * @return the series of arithmetic means, of length
     *         {@code this.length()-periods+1}
     * @throws IllegalArgumentException
     *             if the {@code periods} argument is not positive
     * @see Series#sma(int)
     */
    default Series sma(int periods) {
        return closes().sma(periods);
    }

    /**
     * Returns the exponential moving average over the closing prices of the
     * current {@code Quotes}.
     * <p>
     * The exponential moving average is commonly abbreviated as EMA. See
     * {@link Series#ema(int)} for more details.<br>
     * The method is effectively equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.closes().ema(periods)}
     * </pre>
     * 
     * @param periods
     *            the moving average period
     * @return the series of length {@code this.length()-periods+1}
     */
    default Series ema(int periods) {
        return closes().ema(periods);
    }

    /**
     * Returns the double exponential moving average over the closing prices of
     * the current {@code Quotes}.
     * <p>
     * The double exponential moving average is commonly abbreviated as DEMA.
     * See {@link Series#dema(int)} for more information about this
     * average.<br>
     * The method is effectively equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.closes().dema(periods)}
     * </pre>
     * 
     * @param periods
     *            the moving average period
     * @return the series of length {@code this.length() - 2*periods + 2}
     * @see Series#dema(int)
     */
    default Series dema(int periods) {
        return closes().dema(periods);
    }

    /**
     * Returns the triple exponential moving average over the closing prices of
     * the current {@code Quotes}.
     * <p>
     * The triple exponential moving average is commonly abbreviated as TEMA.
     * See {@link Series#tema(int)} for more information about this
     * average.<br>
     * The method is effectively equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.closes().tema(periods)}
     * </pre>
     * 
     * @param periods
     *            the moving average period
     * @return the series of length {@code this.length() - 3*periods + 3}
     * @see Series#tema(int)
     */
    default Series tema(int periods) {
        return closes().tema(periods);
    }

    /**
     * Gives the triangular moving average of the series.
     * <p>
     * The triangular moving average is sometimes abbreviated as TMA.<br>
     * See the {@link Series#tma(int)} method for more information about this
     * indicator. The TMA is calculated using the closing prices only. The
     * method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.closes().tma(periods)}
     * </pre>
     * 
     * @param periods
     *            the number of bars consisting the indicator averaging period
     * @return the time series of length {@code n - periods + 1}, where
     *         {@code n} is the length of {@code this} series
     */
    default Series tma(int periods) {
        return closes().tma(periods);
    }

    /**
     * Computes the Wilders Moving Average over the closing prices of
     * {@code this}.
     * <p>
     * This method is equivalent to {@code closes().wilders(periods)}. See
     * {@link Series#wilders(int)} for additional information.
     * 
     * @param periods
     *            the moving average smoothing parameter
     * @return the Wilders Moving Average series of length {@code n-periods+1},
     *         where {@code n} is the length of {@code this} series
     */
    default Series wilders(int periods) {
        return closes().wilders(periods);
    }

    /**
     * Applies the provided function to each element in this series.
     * <p>
     * The function application result is returned as a new {@link Series}
     * object with preserved indexation order of elements.
     * 
     * 
     * @param f
     *            the function to be applied to each {@code Quote} in this
     *            {@code Quotes}
     * @return the new time series
     * @throws NullPointerException
     *             if {@code f} is {@code null}
     */
    Series map(ToDoubleFunction<Quote> f);

    /**
     * Computes the <i>weighted close</i> indicator used in technical analysis
     * to approximate the average price of each bar period.
     * <p>
     * The weighted close is sometimes abbreviated as WC.<br>
     * The WC is a statistical indicator.<br>
     * The WC is calculated using high, low and close prices.<br>
     * The WC is similar to {@link #typicalPrice()} but places greater weighting
     * on close price. It may be used in custom calculations as a replacement
     * for {@link #closes()}. The returned time series length matches exactly
     * this quote's {@link #length()}.<br>
     * The method is effectively equivalent to:
     * 
     * <pre>
     * {@code
     * this.map(Quote::weightedClose);
     * }
     * </pre>
     * 
     * @return the time series consisting of weighted close price (i.e.
     *         {@code (2*C + H + L)/4}) of each corresponding quote
     * @see #typicalPrice()
     * @see #medianPrice()
     * @see Quote#weightedClose()
     */
    default Series weightedClose() {
        return map(Quote::weightedClose);
    }

    /**
     * Computes the <i>average price</i> indicator used in technical analysis to
     * approximate the average price of each bar period.
     * <p>
     * The average price is a statistical indicator.<br>
     * The average price is calculated using open, high, low and close prices.<br>
     * The average price may be used in custom calculations as a replacement for
     * {@link #closes()}. The returned time series length matches exactly this
     * quote's {@link #length()}.<br>
     * The default implementation is equivalent to, for this {@code quotes}:
     * 
     * <pre>
     * {@code
     * quotes.map(Quote::averagePrice);
     * }
     * </pre>
     * 
     * @return the time series consisting of the average of the open, high, low and
     *         close prices of each corresponding quote
     * @see #medianPrice()
     * @see #typicalPrice()
     * @see #weightedClose()
     * @see Quote#averagePrice()
     */
    default Series averagePrice() {
        return map(Quote::averagePrice);
    }

    /**
     * Computes the <i>typical price</i> indicator used in technical analysis to
     * approximate the average price of each bar period.
     * <p>
     * The typical price is a statistical indicator.<br>
     * The typical price is calculated using high, low and close prices.<br>
     * The typical price may be used in custom calculations as a replacement for
     * {@link #closes()}. The returned time series length matches exactly this
     * quote's {@link #length()}.<br>
     * The default implementation is equivalent to, for this {@code quotes}:
     * 
     * <pre>
     * {@code
     * quotes.map(Quote::typicalPrice);
     * }
     * </pre>
     * 
     * @return the time series consisting of the average of the high, low and
     *         close prices of each corresponding quote
     * @see #medianPrice()
     * @see #weightedClose()
     * @see Quote#typicalPrice()
     */
    default Series typicalPrice() {
        return map(Quote::typicalPrice);
    }

    /**
     * Computes the <i>median price</i> indicator used in technical analysis to
     * approximate the average price of each bar period.
     * <p>
     * The median price is a statistical indicator.<br>
     * The median price is calculated using high and low prices.<br>
     * The median price may be used in custom calculations as a replacement for
     * {@link #closes()}. The returned time series length matches exactly this
     * quote's {@link #length()}.<br>
     * The default implementation is equivalent to, for this {@code quotes}:
     * 
     * <pre>
     * {@code
     * quotes.map(Quote::medianPrice);
     * }
     * </pre>
     * 
     * @return the time series consisting of the average of the high and low
     *         prices of each corresponding quote
     * @see #typicalPrice()
     * @see Quote#medianPrice()
     */
    default Series medianPrice() {
        return map(Quote::medianPrice);
    }

    /**
     * Returns the series of open prices from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The returned series has exceptional behavior of the
     * {@link Series#ref(int)} method, allowing for {@code opens.ref(1)} call
     * and thus effectively allowing to peek into the nearest future open price.
     * This feature is particularly useful for development of some user-designed
     * strategies or trading systems, see the <i>Trading System Builder's
     * Manual</i> for more information. The <u>{@code quotes.opens().ref(1)}</u>
     * exhibits the following behavior:
     * <ul>
     * <li>The {@code opens.ref(1)} has the same length as the {@code opens}
     * series
     * <li>The value of {@code opens.ref(1).get(barNo)} equals to
     * {@code opens.get(barNo-1)}, which is equivalent to
     * {@code quotes.get(barNo-1).open}
     * <li>The value of {@code opens.ref(1).get(0)} equals to
     * {@code quotes.get(0).close}, where {@code quotes} is the bar sequence
     * from which the {@code opens} series was derived.
     * </ul>
     * Except the mentioned {@code ref(1)} method behavior, this method is
     * equivalent to:
     * 
     * <pre>
     * {@code this.map(q -> q.open)}
     * </pre>
     * 
     * @return a single time series consisting of the open prices of the quotes
     */
    default Series opens() {
        double[] z = new double[length()];
        for (int i = 0; i < z.length; i++)
            z[i] = get(i).open;

        return new DenseSeries(getTimeFrame(), z) {
            @Override
            public Series ref(int periods) {
                if (periods == 1) {
                    double[] result = toArray();
                    if (result.length > 0) {
                        System.arraycopy(result, 0, result, 1, result.length - 1);
                        result[0] = Quotes.this.get(0).close;
                    }
                    return Series.of(getTimeFrame(), result);
                }
                return super.ref(periods);
            }
        };
    }

    /**
     * Returns the series of high prices from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.map(q -> q.high)}
     * </pre>
     * 
     * @return a single time series consisting of the high prices of the quotes
     */
    default Series highs() {
        return map(q -> q.high);
    }

    /**
     * Returns the series of low prices from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.map(q -> q.low)}
     * </pre>
     * 
     * @return a single time series consisting of the low prices of the quotes
     */
    default Series lows() {
        return map(q -> q.low);
    }

    /**
     * Returns the series of closing prices from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.map(q -> q.close)}
     * </pre>
     * 
     * @return a single time series consisting of the closing prices of the
     *         quotes
     */
    default Series closes() {
        return map(q -> q.close);
    }

    /**
     * Returns the series of volume values from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.map(q -> q.volume)}
     * </pre>
     * 
     * @return a single time series consisting of the volume values of the
     *         quotes
     */
    default Series volumes() {
        return map(q -> q.volume);
    }

    /**
     * Returns the series of open interests values from the {@code Quotes}.
     * <p>
     * The returned series has length {@code this.length()}.<br>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.map(q -> q.openInterest)}
     * </pre>
     * 
     * @return a single time series consisting of the open interests values of
     *         the quotes
     */
    default Series openInterests() {
        return map(q -> q.openInterest);
    }

    default Series rsi(int periods) {
        return closes().rsi(periods);
    }

    /**
     * Gives the normal trading range, high to low, including any gap between
     * today's high or low and yesterday's close of the underlying bars.
     * <p>
     * The True Range is a volatility indicator developed by Welles Wilder.<br>
     * The indicator is computed using high, low and close prices.<br>
     * The trading range is measured as an absolute price difference (not in a
     * percentage change).
     * 
     * @return the single time series of length {@code this.length()-1} with
     *         maximum difference between the current high, low and previous
     *         close prices
     * @see #atr(int)
     */
    Series trueRange();

    /**
     * Computes the Average True Range indicator.
     * <p>
     * The Average True Range is commonly abbreviated as ATR.<br>
     * The ATR is a volatility indicator developed by Welles Wilder.<br>
     * The ATR is computed using high, low and close prices.<br>
     * The method is effectively equivalent to, for {@code this} series:
     * 
     * <pre>
     * {@code this.trueRange().wilders(periods)}
     * </pre>
     * 
     * @param periods
     *            the indicator averaging period
     * @return the single time series of length {@code this.length()-periods+1}
     *         with the Wilder's moving average of the true range
     * @throws IllegalArgumentException
     *             if {@code periods} parameter is not positive
     * @see #atrp(int)
     * @see #trueRange()
     */
    default Series atr(int periods) {
        return trueRange().wilders(periods);
    }

    default boolean isUndefined(int i) {
        return i < 0 || i >= length();
    }

    /**
     * Finds the index of the bar with the given date and time.
     * <p>
     * The method is equivalent to, for {@code this} quotes:
     * 
     * <pre>
     * {@code this.findIndex(Chronological.toEpochMicro(datetime))}
     * </pre>
     * 
     * @param datetime
     *            the date and time to locate
     * @return the bar index
     */
    default int findIndex(LocalDateTime datetime) {
        return findIndex(Chronological.toEpochMicro(datetime));
    }

    /**
     * Finds the index of the bar with the given epoch-micros timestamp.
     * 
     * @param epochMicro
     *            the date and time to locate, represented as a number of
     *            microseconds since the epoch
     * @return the bar index
     */
    int findIndex(long epochMicro);
    
}