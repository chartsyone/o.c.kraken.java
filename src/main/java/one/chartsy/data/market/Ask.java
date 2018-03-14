/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import one.chartsy.Symbol;

/**
 * Represents the ask price tick
 * 
 * @author Mariusz Bernacki
 *
 */
public class Ask extends AbstractTick {
    /** The serial version UID */
    private static final long serialVersionUID = -8269784622878805793L;

    /**
     * Constructs a new ask price tick with the specified arguments.
     * 
     * @param symbol
     *            the symbol
     * @param time
     *            the tick timestamp
     * @param price
     *            the ask price
     * @param volume
     *            the volume on the ask side
     */
    public Ask(Symbol symbol, long time, double price, double volume) {
        super(symbol, time, price, volume);
    }

    @Override
    public Type getTickType() {
        return Type.QUOTE;
    }
}
