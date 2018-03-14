/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import one.chartsy.time.Chronological;

/**
 * A symbol's price record also known as "bar" in a particular time frame. Both
 * the {@code TimeFrame} and the {@code Symbol} definitions are not part of the
 * {@code Quote} instance, but instead they are defined in an object wrapping an
 * array of quotes - the {@link Quotes} object - representing a continuous,
 * chronologically ordered series of price changes.
 * 
 * <p>
 * Each price record specifies a set of symbol's price values ({@link #open},
 * {@link #high}, {@link #low}, {@link #close}), an optional {@link #volume} and
 * an optional {@link #openInterest}. Every price record has an associated
 * {@link #time timestamp} representing the moment in time when the price bar
 * occurred, down to microsecond accuracy. If the time frame used is not a
 * <i>tick</i> but represents some continuous time span (e.g. 15 minutes), the
 * {@link #time} reflects the <b>end time</b> of each time frame period (i.e. the
 * ending time of forming the particular bar). If time frame period is a
 * multiple of {@link TimeFrame.Period#DAILY daily period}, then a time
 * component is irrelevant and might be missing in the {@link #time} field.
 * 
 * <p>
 * It should be emphasized that the {@link Quote#time} is an internal
 * representation of time down to microsecond accuracy, and as such is not an
 * equivalent of a Unix timestamp or a Java millis-from-the-epoch. Use
 * {@link Chronological#toDateTime(long)} and
 * {@link Chronological#toEpochMicro(LocalDateTime)} to convert between internal
 * representation and Java date/time objects.
 * 
 * @author Mariusz Bernacki
 * @see Quotes
 */
public final class Quote implements Chronological, Serializable, Cloneable, Comparable<Quote> {
    /** The serial version UID. */
    private static final long serialVersionUID = 2305104990835306584L;
    /** The quote opening price. */
    public final double open;
    /** The quote highest price. */
    public final double high;
    /** The quote lowest price. */
    public final double low;
    /** The quote closing price. */
    public final double close;
    /** The quote volume. */
    public final double volume;
    /** The quote open interests. */
    public final int openInterest;
    /** The ending time of this Quote (UTC Time Zone). */
    public final long time;


    /**
     * Constructs a new {@code Quote} from the specified parameters.
     * 
     * @param time
     *            the quote date and time
     * @param price
     *            the quote price
     */
    public Quote(long time, double price) {
        this(time, price, price, price, price, 0, 0);
    }

    /**
     * Constructs a new {@code Quote} from the specified parameters.
     * 
     * @param time
     *            the quote date and time
     * @param price
     *            the quote price
     * @param volume
     *            the quote volume
     */
    public Quote(long time, double price, double volume) {
        this(time, price, price, price, price, volume, 0);
    }

    /**
     * Constructs a new {@code Quote} from the specified parameters.
     * 
     * @param time
     *            the quote date and time
     * @param open
     *            the quote open price
     * @param high
     *            the quote high price
     * @param low
     *            the quote low price
     * @param close
     *            the quote close price
     * @param volume
     *            the quote volume
     */
    public Quote(long time, double open, double high, double low, double close, double volume) {
        this(time, open, high, low, close, volume, 0);
    }

    /**
     * Constructs a new {@code Quote} from the specified parameters.
     * 
     * @param time
     *            the quote date and time
     * @param open
     *            the quote open price
     * @param high
     *            the quote high price
     * @param low
     *            the quote low price
     * @param close
     *            the quote close price
     * @param volume
     *            the quote volume
     * @param openInterest
     *            the quote open interests
     */
    public Quote(long time, double open, double high, double low, double close, double volume, int openInterest) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.openInterest = openInterest;
    }

    /**
     * Creates an exact copy of the {@code Quote}.
     * 
     * @return an exact copy of {@code this}
     */
    @Override
    public Quote clone() {
        try {
            return (Quote)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Shouldn't happen", e);
        }
    }

    /**
     * Determines whether the current quote is bullish.
     * 
     * @return {@code true} if {@code open < close}, and {@code false} otherwise
     */
    public boolean isBullish() {
        return (open < close);
    }

    /**
     * Determines whether the current quote is bearish.
     * 
     * @return {@code true} if {@code open > close}, and {@code false} otherwise
     */
    public boolean isBearish() {
        return (open > close);
    }

    /**
     * Determines whether the current quote is a doji bar.
     * 
     * @return {@code true} if {@code open == close}, and {@code false} otherwise
     */
    public boolean isDoji() {
        return open == close;
    }

    /**
     * @return the quote timestamp
     */
    @Override
    public long getTime() {
        return time;
    }

    /**
     * Retrieves the specified element of the current quote date and time. The
     * method is effectively equivalent to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateTime().get(field)}
     * </pre>
     * 
     * @param field
     *            specifies what date or time element to retrieve
     * @return the value obtained from this {@code Quote}s' date/time
     */
    public int getDateValue(ChronoField field) {
        return getDateTime().get(field);
    }

    /**
     * Returns the quote year, such as 2017. The method is equivalent to, for
     * the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.YEAR)}
     * </pre>
     * 
     * @return the current quote year
     */
    public int year() {
        return getDateValue(ChronoField.YEAR);
    }

    /**
     * Returns the quote month as a number from January (1) to December
     * (12). The method is equivalent to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.MONTH_OF_YEAR)}
     * </pre>
     * 
     * @return the current quote month (e.g. 12)
     */
    public int month() {
        return getDateValue(ChronoField.MONTH_OF_YEAR);
    }

    /**
     * Returns the quote day of the week from Monday (1) to Sunday (7). The
     * method is equivalent to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.DAY_OF_WEEK)}
     * </pre>
     * 
     * @return the current quote day-of-week
     */
    public int dayOfWeek() {
        return getDateValue(ChronoField.DAY_OF_WEEK);
    }

    /**
     * Returns the quote day of the year (e.g. 365). The method is equivalent
     * to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.DAY_OF_YEAR)}
     * </pre>
     * 
     * @return the current quote day-of-year
     */
    public int dayOfYear() {
        return getDateValue(ChronoField.DAY_OF_YEAR);
    }

    /**
     * Returns the current day of the month (e.g. 30). The method is equivalent
     * to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.DAY_OF_MONTH)}
     * </pre>
     * 
     * @return the current day of the month
     */
    public int day() {
        return getDateValue(ChronoField.DAY_OF_MONTH);
    }

    /**
     * Returns the hour within the day (e.g. 17). The method is equivalent to,
     * for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.HOUR_OF_DAY)}
     * </pre>
     * 
     * @return the current quote hour
     */
    public int hour() {
        return getDateValue(ChronoField.HOUR_OF_DAY);
    }

    /**
     * Returns the quote minutes within the hour, from 0 to 59. The method is
     * equivalent to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.MINUTE_OF_HOUR)}
     * </pre>
     * 
     * @return the current quote minute
     */
    public int minutes() {
        return getDateValue(ChronoField.MINUTE_OF_HOUR);
    }

    /**
     * Returns the quote seconds within the minute, from 0 to 59. The method is
     * equivalent to, for the quote {@code q}:
     * 
     * <pre>
     * {@code q.getDateValue(ChronoField.SECOND_OF_MINUTE)}
     * </pre>
     * 
     * @return the current quote second
     */
    public int seconds() {
        return getDateValue(ChronoField.SECOND_OF_MINUTE);
    }

    /**
     * Computes the weighted close of this bar. The method is equivalent to
     * {@code (2*close + high + low)/4}.
     * 
     * @return the weighted close of the quote
     */
    public double weightedClose() {
        return (2*close + high + low) / 4;
    }

    /**
     * Computes the average price of this bar. The method is equivalent to
     * {@code (open + high + low + close)/4}.
     * 
     * @return the average price of the quote
     */
    public double averagePrice() {
        return (open + high + low + close) / 4;
    }

    /**
     * Computes the typical price of this bar. The method is equivalent to
     * {@code (close + high + low)/3}.
     * 
     * @return the typical price of the quote
     */
    public double typicalPrice() {
        return (close + high + low) / 3;
    }

    /**
     * Computes the median price of this bar. The method is equivalent to
     * {@code (high + low)/2}.
     * 
     * @return the median price of the quote
     */
    public double medianPrice() {
        return (high + low) / 2;
    }

    /**
     * @return the JSON-compatible string representation of the {@code Quote}
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        // Start with the DateTime attribute header
        buff.append('\"').append(getDateTime()).append('\"');
        // Append the OHLC price array
        buff.append(": {OHLC: [").append(open)
        .append(',').append(high)
        .append(',').append(low)
        .append(',').append(close)
        .append(']');
        // Append an optional volume and openInterests
        if (volume != 0.0)
            buff.append(", V: ").append(volume);
        if (openInterest != 0.0)
            buff.append(", OI: ").append(openInterest);
        buff.append('}');

        return buff.toString();
    }

    /**
     * Compares two Quotes for time ordering.
     * 
     * @param that
     *            the {@code Quote} to be compared
     * @return the value {@code 0} if the date of the argument is equal to this
     *         date; a value less than {@code 0} if this date is before the date
     *         of the argument; and a value greater than {@code 0} if this date
     *         is after the date of the argument
     * @throws NullPointerException
     *             if {@code that} is null
     */
    @Override
    public int compareTo(Quote that) {
        return Long.compare(this.time, that.time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Double.hashCode(close)
                ^ Double.hashCode(high)
                ^ Double.hashCode(low)
                ^ (31 * Double.hashCode(open))
                ^ (37 * Double.hashCode(volume))
                ^ (41 * Long.hashCode(time))
                ^ (47 * openInterest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quote) {
            Quote q = (Quote) obj;
            return (time == q.time)
                    && eq(close, q.close)
                    && eq(high, q.high)
                    && eq(low, q.low)
                    && eq(open, q.open)
                    && eq(volume, q.volume)
                    && (openInterest == q.openInterest);
        }
        return false;
    }

    private static boolean eq(double v, double x) {
        return Double.doubleToLongBits(v) == Double.doubleToLongBits(x);
    }
}
