/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A {@code TimeFrame} declares a granularity of candlesticks (bars). Usually a
 * time-based granularity is used represented by a {@link Period} enum or a
 * {@link Custom} instance.
 * 
 * @author Mariusz Bernacki
 *
 */
/*
 * TODO: dailyAlignment: The hour of the day (in the specified timezone) to use
 * for granularities that have daily alignments. [default=17, minimum=0,
 * maximum=23]
 * 
 * TODO: dailyAlignmentTimeZone/alignmentTimeZone: The timezone to use for the
 * dailyAlignment parameter. Candlesticks with daily alignment will be aligned
 * to the dailyAlignment hour within the alignmentTimezone.
 * [default=America/New_York]
 * 
 * TODO: weeklyAlignment: The day of the week used for granularities that have
 * weekly alignment. [default=Friday]
 */
public interface TimeFrame extends java.io.Serializable {

    public static enum Unit {
        SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS;

        public int getMonths(int n) {
            switch (this) {
            case MONTHS:
                return n;
            default:
                return 0;
            }
        }

        public int getSeconds(int n) {
            switch (this) {
            case SECONDS:
                return n;
            case MINUTES:
                return 60*n;
            case HOURS:
                return 3600*n;
            case DAYS:
                return 24*3600*n;
            case WEEKS:
                return 7*24*3600*n;
            default:
                return 0;
            }
        }

        @Override
        public String toString() {
            String name = name();
            return name.substring(0, 1).concat(name.substring(1).toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Provides a predefined set of time-based candlestick granularities.
     * <p>
     * The values in this {@code enum} are organized into two partitions - one
     * daily-based (i.e. daily, weekly, monthly,... etc), followed by an intraday,
     * seconds-based partition. The values in each partition are ordered from the
     * smallest to the largest time frames. The intraday partition contains all
     * intraday time frames that align nicely into a 24-hour time span (except some
     * missing from seconds-based range).
     * 
     * 
     * @author Mariusz Bernacki
     *
     */
    static enum Period implements TimeFrame {
        /** The 1 day bars, day alignment. */
        DAILY(86400, 0, "Daily"),
        /** The 1 week bars, aligned to start of a week. */
        WEEKLY(604800, 0, "Weekly"),
        /** The 1 month bars, aligned to first day of a month. */
        MONTHLY(0, 1, "Monthly"),
        /** The 1 quarter bars, aligned to first day of a quarter. */ // TODO
        QUARTERLY(0, 3, "Quarterly"),
        /** The 1 year bars, aligned to first day of a year. */ // TODO
        YEARLY(0, 12, "Yearly"),
        /** The 1 second bars, can be also interpreted as and used instead of ticks. */
        S1(1, 0, "S1"),
        /** The 5 second bars with a minute alignment. */
        S5(5, 0, "S5"),
        /** The 10 second bars with a minute alignment. */
        S10(10, 0, "S10"),
        /** The 15 second bars with a minute alignment. */
        S15(15, 0, "S15"),
        /** The 30 second bars with a minute alignment. */
        S30(30, 0, "S30"),
        /** The 1 minute bars with a minute alignment.*/
        M1(60, 0, "M1"),
        /** The 2 minute bars with an hour alignment. */
        M2(120, 0, "M2"),
        /** The 3 minute bars with an hour alignment. */
        M3(180, 0, "M3"),
        /** The 4 minute bars with an hour alignment. */
        M4(240, 0, "M4"),
        /** The 5 minute bars with an hour alignment. */
        M5(300, 0, "M5"),
        /** The 6 minute bars with an hour alignment. */
        M6(360, 0, "M6"),
        /** The 10 minute bars with an hour alignment. */
        M10(600, 0, "M10"),
        /** The 12 minute bars with an hour alignment. */
        M12(720, 0, "M12"),
        /** The 15 minute bars with an hour alignment. */
        M15(900, 0, "M15"),
        /** The 20 minute bars with an hour alignment. */
        M20(1200, 0, "M20"),
        /** The 30 minute bars with an hour alignment. */
        M30(1800, 0, "M30"),
        /** The 45 minute bars with a day alignment. */
        M45(2700, 0, "M45"),
        /** The 1 hour bars with an hour alignment. */
        H1(3600, 0, "H1"),
        /** The 90 minute bars with a day alignment. */
        M90(5400, 0, "M90"),
        /** The 2 hour bars with a day alignment. */
        H2(7200, 0, "H2"),
        /** The 3 hour bars with a day alignment. */
        H3(10800, 0, "H3"),
        /** The 4 hour bars with a day alignment. */
        H4(14400, 0, "H4"),
        /** The 6 hour bars with a day alignment. */
        H6(21600, 0, "H6"),
        /** The 8 hour bars with a day alignment. */
        H8(28800, 0, "H8"),
        /** The 12 hour bars with a day alignment. */
        H12(43200, 0, "H12");

        /** The underlying custom time frame object. */
        private final Custom timeFrame;

        /**
         * The private enum time frame constructor.
         * 
         * @param seconds
         *            the time frame duration in seconds
         * @param months
         *            the time frame duration in months
         * @param code
         *            the time frame code
         */
        private Period(int seconds, int months, String code) {
            timeFrame = new Custom(seconds, months, code, code);
        }

        @Override
        public int seconds() {
            return timeFrame.seconds;
        }

        @Override
        public int months() {
            return timeFrame.months;
        }

        @Override
        public boolean isIntraday() {
            return timeFrame.isIntraday();
        }

        @Override
        public boolean isAssignableFrom(TimeFrame tf) {
            return timeFrame.isAssignableFrom(tf);
        }

        @Override
        public String getCode() {
            return name();
        }

        @Override
        public String toString() {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(getClass().getPackage().getName() + ".Bundle");
                return bundle.getString("TimeFrame." + getCode());
            } catch (MissingResourceException e) {
                return getCode();
            }
        }

        /**
         * Returns an iterable over the elements of this
         * {@code TimeFrame.Period} in reverse sequential order.
         * <p>
         * The elements will be returned in order from the largest time frame (
         * {@link #YEARLY}) to the smallest ({@link #TICK}).
         *
         * @return an iterable over the defined time frames in reverse order
         */
        public static Iterable<TimeFrame.Period> descendingValues() {
            Deque<TimeFrame.Period> descValues = new ArrayDeque<>(Arrays.asList(values()));
            return descValues::descendingIterator;
        }
    }

    List<TimeFrame> IntradayTimeFrames = Collections.unmodifiableList(Arrays.<TimeFrame>asList(
            Period.M1,
            Period.M2,
            Period.M3,
            Period.M4,
            Period.M5,
            Period.M6,
            Period.M10,
            Period.M12,
            Period.M15,
            Period.M20,
            Period.M30,
            Period.M45,
            Period.H1,
            Period.M90,
            Period.H2,
            Period.H3,
            Period.H4,
            Period.H6,
            Period.H8,
            Period.H12
            ));

    /**
     * The custom implementation of the time frame.
     * 
     * @author Mariusz Bernacki
     *
     */
    public static class Custom implements TimeFrame {

        private static final long serialVersionUID = 1891772192859326556L;
        /** The code of this time frame. */
        private final String code;
        /** The name of this custom time frame. */
        private final String name;
        /** The number of seconds in the period. */
        protected final int seconds;
        /** The number of months in the period. */
        protected final int months;

        /**
         * Constructs a custom time frame with the given parameters.
         * 
         * @param duration
         *            the time frame duration in the given {@code unit}'s
         * @param unit
         *            the time frame duration unit
         * @param code
         *            the time frame code
         * @param name
         *            the custom time frame name
         */
        public Custom(int duration, Unit unit, String code, String name) {
            this(unit.getSeconds(duration), unit.getMonths(duration), code, name);
        }

        /**
         * Constructs a custom time frame with the given parameters.
         * 
         * @param seconds
         *            the time frame duration in seconds
         * @param monts
         *            the time frame duration in months
         * @param code
         *            the time frame code
         * @param name
         *            the custom time frame name
         */
        Custom(int seconds, int months, String code, String name) {
            if (seconds < 0)
                throw new IllegalArgumentException(
                        "\"Seconds\" argument in a custom Time Frame must be non-negative however "
                                + seconds + " was provided.");
            if (months < 0)
                throw new IllegalArgumentException(
                        "\"Months\" argument in a custom Time Frame must be non-negative however "
                                + months + " was provided.");
            if (seconds > 0 && months > 0)
                throw new IllegalArgumentException(
                        "Either \"Seconds\" or \"Months\" argument in a custom Time Frame must be zero however "
                                + seconds + "s and " + months + "m was provided.");

            this.seconds = seconds;
            this.months = months;
            this.code = code;
            this.name = name;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int seconds() {
            return seconds;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int months() {
            return months;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isIntraday() {
            return months == 0 && seconds < 86400;
        }

        @Override
        public boolean isAssignableFrom(TimeFrame tf) {
            int months = tf.months();
            int seconds = tf.seconds();

            if (this.months > 0) {
                if (months == 0)
                    return (seconds > 0 && Period.DAILY.seconds() % seconds == 0);
                return (this.months % months == 0);
            } else if (seconds == 0) {
                return this.seconds == 0;
            } else {
                return this.seconds % seconds == 0;
            }
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static final java.util.Comparator<TimeFrame> Comparator = new java.util.Comparator<TimeFrame>() {

        @Override
        public int compare(TimeFrame o1, TimeFrame o2) {
            int months = o1.months() - o2.months();
            if (months != 0)
                return months;

            return o1.seconds() - o2.seconds();
        }
    };

    /**
     * Returns the time frame duration in seconds. For the
     * {@link #PERIOD_CURRENT} <tt>0</tt> is returned.
     * 
     * @return the number of seconds in the current period
     */
    public int seconds();

    /**
     * Returns the time frame duration in months. For the
     * {@link #PERIOD_CURRENT} <tt>0</tt> is returned.
     * 
     * @return the number of months in the current period
     */
    public int months();

    public boolean isAssignableFrom(TimeFrame tf);

    public boolean isIntraday();

    public String getCode();

    /**
     * Checks that the specified series have the same time frame. This method is designed
     * primarily for doing parameter validation in methods and constructors, as
     * demonstrated below:
     * <pre>
     * public static Series combine(Series first, Series other) {
     *     TimeFrame.requireSame(first, other);
     *
     *     double[] result = new double[Math.min(first.length(), other.length())];
     *     for (int i = result.length-1; i &gt;= 0; i--)
     *         result[i] = ...
     *     ...
     * }
     * </pre>
     *
     * @param s1
     *            the series to match against the other one
     * @param s2
     *            the series to match against the other one
     * @throws IllegalArgumentException
     *             if {@code s1.getTimeFrame()} does not match
     *             {@code s2.getTimeFrame()}
     */
    public static void requireSame(Series s1, Series s2) {
        if (s1.getTimeFrame().seconds() != s2.getTimeFrame().seconds())
            throw new IllegalArgumentException("Time frame mismatch");
    }
}
