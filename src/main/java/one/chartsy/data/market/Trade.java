/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import one.chartsy.Symbol;

/**
 * Represents a trade tick data.
 * <p>
 * Trade ticks are timestamped to the nearest microsecond.
 * 
 * @author Mariusz Bernacki
 * 
 */
public class Trade extends AbstractTick {
    /** The serial version UID */
    private static final long serialVersionUID = 5752959757378672752L;
    /** The direction of the trade. */
    private final Direction direction;


    /**
     * Constructs a new trade from the specified arguments.
     * 
     * @param symbol
     *            the symbol
     * @param time
     *            the tick timestamp
     * @param price
     *            the price of the last trade
     * @param volume
     *            the last trade volume
     * @param dir
     *            the direction of the trade (buy or sell)
     */
    public Trade(Symbol symbol, long time, double price, double volume, Direction dir) {
        super(symbol, time, price, volume);
        this.direction = (dir == null)? Direction.UNKNOWN : dir;
    }

    /**
     * Returns the direction of the trade (buy or sell).
     * 
     * @return the trade direction
     */
    public Direction getDirection() {
        return direction;
    }

    @Override
    public final Type getTickType() {
        return Type.TRADE;
    }

    @Override
    public String toString() {
        return getSymbol() + ": {\"" + getDateTime() + "\": {" + getPrice() + ", " + getVolume() + ", " + getDirection() + "}}";
    }

    /**
     * The trade direction (buy ask) or (sell bid)
     * 
     * @author Mariusz Bernacki
     *
     */
    public enum Direction {
        UNKNOWN, BUY_ASK, SELL_BID;

        public static Direction parse(String token, String buyAskConst, String sellBidConst) {
            if (buyAskConst.equals(token))
                return BUY_ASK;
            else if (sellBidConst.equals(token))
                return SELL_BID;
            else
                return UNKNOWN;
        }
    }
}
