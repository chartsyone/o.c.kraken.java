/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import java.util.List;

import one.chartsy.Symbol;

/**
 * Reflects the Level 2 data (order book) snapshot.
 * 
 * @author Mariusz Bernacki
 *
 */
public class Level2Snapshot {
    /** The symbol for which this market snapshot is generated. */ 
    private final Symbol symbol;
    /** The timestamp of the snapshot. */
    private final long time;
    /** The list of bid price levels. */
    private final List<Bid> bids;
    /** The list of ask price levels. */
    private final List<Ask> asks;


    public Level2Snapshot(Symbol symbol, long time, List<Bid> bids, List<Ask> asks) {
        if (symbol == null)
            throw new IllegalArgumentException("The \"symbol\" argument cannot be NULL");
        if (bids == null)
            throw new IllegalArgumentException("The \"bids\" list cannot be NULL");
        if (asks == null)
            throw new IllegalArgumentException("The \"asks\" list cannot be NULL");

        this.symbol = symbol;
        this.time = time;
        this.bids = bids;
        this.asks = asks;
    }

    /**
     * @return the symbol
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the bids
     */
    public List<Bid> getBids() {
        return bids;
    }

    /**
     * @return the asks
     */
    public List<Ask> getAsks() {
        return asks;
    }
}
