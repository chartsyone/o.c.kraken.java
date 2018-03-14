/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import one.chartsy.Quote;
import one.chartsy.Symbol;
import one.chartsy.time.Chronological;

/**
 * Represents the market tick event, either trade or quote based.
 * <p>
 * Ticks are timestamped to the nearest microsecond in Chartsy|One.
 * 
 * @author Mariusz Bernacki
 *
 */
public interface Tick extends Chronological {

    /**
     * Returns the symbol to which this tick belongs.
     * 
     * @return the symbol, not null
     */
    Symbol getSymbol();

    /**
     * Returns the price of this tick.
     * 
     * @return the tick price
     */
    double getPrice();

    /**
     * Returns the volume of the tick.
     * 
     * @return the tick volume
     */
    double getVolume();

    /**
     * Returns the timestamp at which this tick was recorded.
     * 
     * @return the tick timestamp
     */
    @Override
    long getTime();

    /**
     * Gives the type of the tick event (QUOTE or TRADE).
     * 
     * @return the type of this tick
     */
    Type getTickType();

    /**
     * Converts this tick to a quote (candle) form.
     * <ul>
     * <li>candle's time equal to tick timestamp,</li>
     * <li>candle's OHLC prices all the same and equal to tick price,</li>
     * <li>candle's volume equal to tick volume,</li>
     * <li>tick symbol ignored.</li>
     * </ul>
     * 
     * @return this tick as a candle
     */
    default Quote toQuote() {
        return new Quote(getTime(), getPrice(), getVolume());
    }

    /**
     * Represents a type of tick data.
     * 
     * @author Mariusz Bernacki
     *
     */
    public enum Type {
        /** The last trades ticks. */
        QUOTE, TRADE;
    }
}
