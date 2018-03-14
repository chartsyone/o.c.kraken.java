/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;

/**
 * Interface represents an item that is defined, in part, by a point in time.
 * <p>
 * This software internally across the whole library uses a machine
 * representation of the time stored as a {@code long} number, and based on a
 * number of microseconds elapsed since the Epoch, measured using UTC-zoned
 * clock.
 * 
 * @author Mariusz Bernacki
 *
 */
@FunctionalInterface
public interface Chronological {

    /**
     * The shared instance of default {@code Comparator} that can be used to
     * compare two {@code Chronological} objects.
     * <p>
     * It's <b>important to note</b> that this comparator assumes time series
     * compatible elements ordering, which is from present to past (e.g. array
     * using descending time-stamps for ascending indexes). If your code needs
     * ascending compatible comparator, remember to {@link Comparator#reversed()
     * reverse} the provided comparator.
     */
    Comparator<Chronological> COMPARATOR = Collections.reverseOrder((t1, t2) -> Long.compare(t1.getTime(), t2.getTime()));

    /**
     * Returns an object's time as a number of microseconds since the epoch
     * measured with a UTC time-zoned clock.
     * 
     * @return the object's time, i.e. the quote ending time when using with the
     *         {@code Quote} instances
     */
    long getTime();

    /**
     * Returns an objects's date and time as a {@code LocalDateTime} instance. The
     * method is equivalent to:
     * 
     * <pre>
     * {@code Chronological.toDateTime(this.getTime())}
     * </pre>
     * 
     * @return the {@code LocalDateTime} value
     */
    default LocalDateTime getDateTime() {
        return Chronological.toDateTime(getTime());
    }

    /**
     * Returns an object's date as a {@code LocalDate} instance. The time
     * component present in the current quote is ignored. The method is
     * equivalent to:
     * 
     * <pre>
     * {@code this.getDateTime().toLocalDate()}
     * </pre>
     * 
     * @return the {@code LocalDate} value
     */
    default LocalDate getDate() {
        return getDateTime().toLocalDate();
    }

    /**
     * Converts the {@code time} microseconds since the epoch measured with
     * UTC-zoned clock to a time-zone-free {@code LocalDateTime} object. The
     * result may be converted back to timestamp using the method
     * {@link #toEpochMicro(LocalDateTime)}.
     * 
     * @param epochMicro
     *            the number of microseconds elapsed since the "epoch"
     * @return the {@code LocalDateTime}
     */
    static LocalDateTime toDateTime(long epochMicro) {
        return toDateTime(epochMicro, ZoneOffset.UTC);
    }

    /**
     * Converts the {@code time} microseconds since the epoch measured with
     * UTC-zoned clock to a {@code LocalDateTime} at the specified time zone.
     * 
     * @param epochMicro
     *            the number of microseconds elapsed since the "epoch"
     * @param zoneId
     *            the target time zone of the result
     * @return the {@code LocalDateTime}
     */
    static LocalDateTime toDateTime(long epochMicro, ZoneId zoneId) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(Math.floorDiv(epochMicro, 1000_000), Math.floorMod(epochMicro, 1000_000) * 1000),
                zoneId);
    }

    /**
     * Converts the specified date and time to its internal timestamp
     * representation. The result may be converted back to {@code LocalDateTime}
     * using the method {@link #toDateTime(long)}.
     * <p>
     * If the {@code datetime} represents a point in time too far in the future or
     * past to fit in a {@code long} microseconds, then an
     * {@code ArithmeticException} is thrown. If the {@code datetime} has greater
     * than microsecond precision, then the conversion will drop any excess
     * precision information as though the amount in nanoseconds was subject to
     * integer division by one thousand.
     * 
     * @param datetime
     *            the date and time to convert
     * @return the number of microseconds elapsed since the epoch of
     *         1970-01-01T00:00:00Z
     */
    static long toEpochMicro(LocalDateTime datetime) {
        Instant instant = datetime.toInstant(ZoneOffset.UTC);
        return Math.addExact(Math.multiplyExact(instant.getEpochSecond(), 1000_000), instant.getNano()/1000);
    }
}
