/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import one.chartsy.Symbol;

public abstract class AbstractTick implements java.io.Serializable, Tick {
    /** The serial version UID */
    private static final long serialVersionUID = -811804959339108399L;
    /** The symbol to which this ticks belongs. */
    private final Symbol symbol;
    /** The price of the last trade. */
    private final double price;
    /** The last trade volume. */
    private final double volume;
    /** The timestamp of the trade. */
    private final long time;


    /**
     * Constructs a new tick from the specified arguments.
     * 
     * @param symbol
     *            the symbol
     * @param time
     *            the tick timestamp
     * @param price
     *            the tick price
     * @param volume
     *            the tick volume
     */
    protected AbstractTick(Symbol symbol, long time, double price, double volume) {
        if (symbol == null)
            throw new IllegalArgumentException("The `symbol` argument cannot be NULL");

        this.symbol = symbol;
        this.time = time;
        this.price = price;
        this.volume = volume;
    }

    @Override
    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public long getTime() {
        return time;
    }
}