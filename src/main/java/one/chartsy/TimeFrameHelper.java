/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import one.chartsy.data.DenseQuotes;
import one.chartsy.time.Chronological;

public class TimeFrameHelper {

    public static Quotes timeFrameCompress(TimeFrame targetTF, Quotes quotes) {
        TimeFrame frame = quotes.getTimeFrame();
        // check if required time frame is already provided
        if (targetTF.seconds() == frame.seconds() && targetTF.months() == frame.months())
            return quotes;
        // check if time frame frequency compression is possible
        if (!targetTF.isAssignableFrom(frame))
            throw new IllegalArgumentException("TimeFrame's mismatch: " + frame + " Time Frame is not compressible to " + targetTF);

        int seconds = targetTF.seconds();
        if (seconds == 0 && targetTF.months() > 0) {
            // need to compress to monthly or higher target
            // compress to daily first to speed up performance
            if (frame.isIntraday() && TimeFrame.Period.DAILY.isAssignableFrom(frame))
                quotes = timeFrameCompress(TimeFrame.Period.DAILY, quotes);
            return timeFrameCompressLarge(targetTF, quotes, targetTF.months());
        }
        long micros = seconds * 1000_000L;
        ArrayList<Quote> buffer = new ArrayList<>();
        boolean compressingFromIntraToDaily = (frame.isIntraday() && !targetTF.isIntraday());
        double close = 0, open = 0, low = 0, high = 0, volume = 0;
        int openInterest = 0;
        long time = 0;
        int size = quotes.length();
        int off = frame.isIntraday()? 1 : 0; // extra intraday time offset
        for (int barNo = size-1; barNo >= 0; barNo--) {
            Quote q = quotes.get(barNo);
            long t = q.time;
            if (t < time && time != 0)
                continue;
            if ((time - reftime - off)/micros != (t - reftime - off)/micros || time == 0) {
                if (time != 0) {
                    if (compressingFromIntraToDaily && (time - reftime) % 86400000000L == 0)
                        time--;
                    buffer.add(new Quote(time, open, high, low, close, volume, openInterest));
                }
                open = q.open;
                high = q.high;
                low = q.low;
                close = q.close;
                volume = q.volume;
                openInterest = q.openInterest;
                time = t;
            } else {
                double h = q.high;
                if (h > high)
                    high = h;
                double l = q.low;
                if (l < low)
                    low = l;
                close = q.close;
                volume += q.volume;
                openInterest = q.openInterest;
                time = t;
            }
        }
        buffer.add(new Quote(time, open, high, low, close, volume, openInterest));
        Quote[] result = new Quote[buffer.size()];
        result = buffer.toArray(result);
        for (int i=0, mid=result.length>>1, j=result.length-1; i<mid; i++, j--) {
            Quote q = result[i];
            result[i] = result[j];
            result[j] = q;
        }
        return new DenseQuotes(quotes.getSymbol(), targetTF, result);
    }

    private static boolean sameBar(Calendar t1, Calendar t2, int months) {
        final int REFERENCE_YEAR = 2001;
        int elapsedMonths1 = 12*(t1.get(Calendar.YEAR) - REFERENCE_YEAR) + t1.get(Calendar.MONTH);
        int elapsedMonths2 = 12*(t2.get(Calendar.YEAR) - REFERENCE_YEAR) + t2.get(Calendar.MONTH);

        return elapsedMonths1 / months == elapsedMonths2 / months;
    }

    private static Quotes timeFrameCompressLarge(TimeFrame targetTF, Quotes quotes, int months) {
        List<Quote> buffer = new ArrayList<>();
        double close = 0, open = 0, low = 0, high = 0, volume = 0;
        int openInterest = 0;
        long time = 0;
        int size = quotes.length();
        Calendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        Calendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        for (int barNo = size-1; barNo >= 0; barNo--) {
            Quote q = quotes.get(barNo);
            long t = q.time;
            if (t < time && time != 0)
                continue;
            cal2.setTimeInMillis(t / 1000);
            if (time == 0 || !sameBar(cal1, cal2, months)) {
                if (time != 0)
                    buffer.add(new Quote(time, open, high, low, close, volume, openInterest));
                open = q.open;
                high = q.high;
                low = q.low;
                close = q.close;
                volume = q.volume;
                openInterest = q.openInterest;
            } else {
                double h = q.high;
                if (h > high)
                    high = h;
                double l = q.low;
                if (l < low)
                    low = l;
                close = q.close;
                volume += q.volume;
                openInterest = q.openInterest;
            }
            cal1.setTimeInMillis((time = t) / 1000);
        }
        buffer.add(new Quote(time, open, high, low, close, volume, openInterest));
        return Quotes.of(quotes.getSymbol(), targetTF, buffer);
    }

    /** The number of milliseconds elapsed since Monday, January 1st, 2001. */
    private static final long reftime = Chronological.toEpochMicro(LocalDateTime.of(2001, 1, 1, 0, 0));

}
