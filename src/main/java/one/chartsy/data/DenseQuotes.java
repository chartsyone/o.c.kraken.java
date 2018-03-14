/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import one.chartsy.Quote;
import one.chartsy.Quotes;
import one.chartsy.Series;
import one.chartsy.Symbol;
import one.chartsy.TimeFrame;
import one.chartsy.time.Chronological;

/**
 * The default implementation of the {@link Quotes} interface.
 * <p>
 * The {@code DenseQuotes} does not implement {@code hashCode} or {@code equals}
 * methods. It uses object identity comparison by design.
 * 
 * @author Mariusz Bernacki
 *
 */
public final class DenseQuotes implements Quotes, Serializable {
    /** Serial UID */
    private static final long serialVersionUID = -6064350045530808717L;
    /** The current time frame. */
    private final TimeFrame timeFrame;
    /** The current symbol. */
    private final Symbol symbol;
    /** The quote data. */
    private transient Quote[] quotes;


    public DenseQuotes(Symbol symbol, TimeFrame timeFrame, Quote[] quotes) {
        this.symbol = symbol;
        this.timeFrame = timeFrame;
        this.quotes = quotes;
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#toArray()
     */
    @Override
    public Quote[] toArray() {
        return Arrays.copyOf(quotes, quotes.length);
    }

    @Override
    public Quotes trimToLength(int newLength) {
        if (length() <= newLength)
            return this;

        return new DenseQuotes(symbol, timeFrame, Arrays.copyOf(quotes, newLength));
    }

    @Override
    public Quote get(int barNo) {
        return quotes[barNo];
    }

    @Override
    public boolean isUndefined(int i) {
        return i < 0 || i >= quotes.length;
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#getSymbol()
     */
    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#getTimeFrame()
     */
    @Override
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    @Override
    public int length() {
        return quotes.length;
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#map(java.util.function.ToDoubleFunction)
     */
    @Override
    public Series map(ToDoubleFunction<Quote> f) {
        double[] z = new double[length()];
        for (int i = z.length-1; i >= 0; i--)
            z[i] = f.applyAsDouble(get(i));

        return Series.of(getTimeFrame(), z);
    }

    @Override
    public Series opens() {
        SeriesCache cache = getCache();
        if (cache.o == null)
            cache.o = Quotes.super.opens();
        return cache.o;
    }

    @Override
    public Series highs() {
        SeriesCache cache = getCache();
        if (cache.h == null)
            cache.h = Quotes.super.highs();
        return cache.h;
    }

    @Override
    public Series lows() {
        SeriesCache cache = getCache();
        if (cache.l == null)
            cache.l = Quotes.super.lows();
        return cache.l;
    }

    @Override
    public Series closes() {
        //		return getCache().get("C", () -> map(q -> q.close));
        SeriesCache cache = getCache();
        if (cache.c == null)
            cache.c = Quotes.super.closes();
        return cache.c;
    }

    @Override
    public Series volumes() {
        SeriesCache cache = getCache();
        if (cache.v == null)
            cache.v = Quotes.super.volumes();
        return cache.v;
    }

    @Override
    public Series openInterests() {
        SeriesCache cache = getCache();
        if (cache.oi == null)
            cache.oi = Quotes.super.openInterests();
        return cache.oi;
    }

    private transient volatile SoftReference<SeriesCache> softCache;

    private SeriesCache getCache() {
        SeriesCache ref = null;
        SoftReference<SeriesCache> cache = this.softCache;
        if (cache == null || (ref = cache.get()) == null)
            this.softCache = new SoftReference<SeriesCache>(ref = new SeriesCache());
        return ref;
    }

    static class SeriesCache {
        Function<Series, Reference<Series>> refFactory = SoftReference<Series>::new;
        Map<String, Reference<Series>> map = new HashMap<>();
        Series o;
        Series h;
        Series l;
        Series c;
        Series v;
        Series oi;

        Series get(String name, Supplier<Series> defaultValue) {
            Series r;
            Reference<Series> value = map.get(name);
            if (value == null || (r = value.get()) == null)
                map.put(name, refFactory.apply(r = defaultValue.get()));
            return r;
        }
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#trueRange()
     */
    @Override
    public Series trueRange() {
        int newLength = length() - 1;
        if (newLength <= 0)
            return Series.empty(timeFrame);

        Quote q2 = get(newLength);
        double[] result = new double[newLength];
        for (int i = newLength - 1; i >= 0; i--) {
            Quote q1 = get(i);
            result[i] = Math.max(q1.high, q2.close) - Math.min(q1.low, q2.close);
            q2 = q1;
        }
        return Series.of(timeFrame, result);
    }

    /* (non-Javadoc)
     * @see com.softignition.chartsy4j.Quotes#toString()
     */
    @Override
    public String toString() {
        return "Quotes[" + symbol + "]";
    }

    @Override
    public int findIndex(long epochMicro) {
        Chronological forTime = () -> epochMicro;
        return Arrays.binarySearch(quotes, forTime, Chronological.COMPARATOR);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(quotes.length);
        for (int barNo = quotes.length - 1; barNo >= 0; barNo--) {
            Quote bar = get(barNo);
            out.writeLong(bar.time);
            out.writeDouble(bar.open);
            out.writeDouble(bar.high);
            out.writeDouble(bar.low);
            out.writeDouble(bar.close);
            out.writeDouble(bar.volume);
            out.writeInt(bar.openInterest);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        quotes = new Quote[in.readInt()];
        for (int barNo = quotes.length - 1; barNo >= 0; barNo--) {
            long time = in.readLong();
            double open = in.readDouble();
            double high = in.readDouble();
            double low = in.readDouble();
            double close = in.readDouble();
            double volume = in.readDouble();
            int openInterest = in.readInt();
            quotes[barNo] = new Quote(time, open, high, low, close, volume, openInterest);
        }
    }
}
