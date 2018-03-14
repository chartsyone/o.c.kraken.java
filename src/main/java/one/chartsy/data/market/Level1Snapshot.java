/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

import one.chartsy.Symbol;

/**
 * Reflects the Level 1 snapshot of the market data.
 * 
 * @author Mariusz Bernacki
 *
 */
public class Level1Snapshot {
    /** The symbol corresponding to this snapshot. */
    private final Symbol symbol;
    /** The bid price and volume. */
    private Bid bid;
    /** The ask price and volume. */
    private Ask ask;
    /** The most recent trade (price and volume). */
    private Trade lastTrade;
    /** The current day volume. */
    private double dayVolume;
    /** The total volume from the last 24 hours. */
    private double rollingDayVolume;
    /** The current day's volume weighted average price. */
    private double dayVwap;
    /** The volume weighted average price of the last 24 hours. */
    private double rollingDayVwap;
    /** The number of trades in the current day. */
    private long dayTradeNumber;
    /** The number of trades from the last 24 hours. */
    private long rollingDayTradeNumber;
    /** The lowest price of the day. */
    private double dayLow;
    /** The lowest price from the last 24 hours. */
    private double rollingDayLow;
    /** The highest price of the day. */
    private double dayHigh;
    /** The highest price from the last 24 hours. */
    private double rollingDayHigh;
    /** The day open price. */
    private double dayOpen;
    /** The snapshot time. */
    private final long time;


    public Level1Snapshot(Symbol symbol, long time) {
        if (symbol == null)
            throw new IllegalArgumentException("symbol is NULL");

        this.symbol = symbol;
        this.time = time;
    }

    /**
     * Returns the symbol corresponding to this snapshot.
     *
     * @return the Symbol
     */
    public final Symbol getSymbol() {
        return symbol;
    }

    /**
     * Returns the bid price and volume.
     *
     * @return the bid
     */
    public Bid getBid() {
        return bid;
    }

    /**
     * Sets a new bid price and volume.
     *
     * @param bid the bid to set
     */
    public void setBid(Bid bid) {
        this.bid = bid;
    }

    /**
     * Returns the ask price and volume.
     *
     * @return the ask
     */
    public Ask getAsk() {
        return ask;
    }

    /**
     * Sets a new ask price and volume.
     *
     * @param ask the ask to set
     */
    public void setAsk(Ask ask) {
        this.ask = ask;
    }

    /**
     * Returns the most recent trade (price and volume).
     *
     * @return the lastTrade
     */
    public Trade getLastTrade() {
        return lastTrade;
    }

    /**
     * Sets a new most recent trade (price and volume).
     *
     * @param lastTrade the lastTrade to set
     */
    public void setLastTrade(Trade lastTrade) {
        this.lastTrade = lastTrade;
    }

    /**
     * Returns the current day volume.
     *
     * @return the dayVolume
     */
    public double getDayVolume() {
        return dayVolume;
    }

    /**
     * Sets a new current day volume.
     *
     * @param dayVolume the dayVolume to set
     */
    public void setDayVolume(double dayVolume) {
        this.dayVolume = dayVolume;
    }

    /**
     * Returns the total volume from the last 24 hours.
     *
     * @return the rollingDayVolume
     */
    public double getRollingDayVolume() {
        return rollingDayVolume;
    }

    /**
     * Sets a new total volume from the last 24 hours.
     *
     * @param rollingDayVolume the rollingDayVolume to set
     */
    public void setRollingDayVolume(double rollingDayVolume) {
        this.rollingDayVolume = rollingDayVolume;
    }

    /**
     * Returns the current day's volume weighted average price.
     *
     * @return the dayVwap
     */
    public double getDayVwap() {
        return dayVwap;
    }

    /**
     * Sets a new current day's volume weighted average price.
     *
     * @param dayVwap the dayVwap to set
     */
    public void setDayVwap(double dayVwap) {
        this.dayVwap = dayVwap;
    }

    /**
     * Returns the volume weighted average price of the last 24 hours.
     *
     * @return the rollingDayVwap
     */
    public double getRollingDayVwap() {
        return rollingDayVwap;
    }

    /**
     * Sets a new volume weighted average price of the last 24 hours.
     *
     * @param rollingDayVwap the rollingDayVwap to set
     */
    public void setRollingDayVwap(double rollingDayVwap) {
        this.rollingDayVwap = rollingDayVwap;
    }

    /**
     * Returns the number of trades in the current day.
     *
     * @return the dayTradeNumber
     */
    public long getDayTradeNumber() {
        return dayTradeNumber;
    }

    /**
     * Sets a new number of trades in the current day.
     *
     * @param dayTradeNumber the dayTradeNumber to set
     */
    public void setDayTradeNumber(long dayTradeNumber) {
        this.dayTradeNumber = dayTradeNumber;
    }

    /**
     * Returns the number of trades from the last 24 hours.
     *
     * @return the rollingDayTradeNumber
     */
    public long getRollingDayTradeNumber() {
        return rollingDayTradeNumber;
    }

    /**
     * Sets a new number of trades from the last 24 hours.
     *
     * @param rollingDayTradeNumber the rollingDayTradeNumber to set
     */
    public void setRollingDayTradeNumber(long rollingDayTradeNumber) {
        this.rollingDayTradeNumber = rollingDayTradeNumber;
    }

    /**
     * Returns the lowest price of the day.
     *
     * @return the dayLow
     */
    public double getDayLow() {
        return dayLow;
    }

    /**
     * Sets a new lowest price of the day.
     *
     * @param dayLow the dayLow to set
     */
    public void setDayLow(double dayLow) {
        this.dayLow = dayLow;
    }

    /**
     * Returns the lowest price from the last 24 hours.
     *
     * @return the rollingDayLow
     */
    public double getRollingDayLow() {
        return rollingDayLow;
    }

    /**
     * Sets a new lowest price from the last 24 hours.
     *
     * @param rollingDayLow the rollingDayLow to set
     */
    public void setRollingDayLow(double rollingDayLow) {
        this.rollingDayLow = rollingDayLow;
    }

    /**
     * Returns the highest price of the day.
     *
     * @return the dayHigh
     */
    public double getDayHigh() {
        return dayHigh;
    }

    /**
     * Sets a new highest price of the day.
     *
     * @param dayHigh the dayHigh to set
     */
    public void setDayHigh(double dayHigh) {
        this.dayHigh = dayHigh;
    }

    /**
     * Returns the highest price from the last 24 hours.
     *
     * @return the rollingDayHigh
     */
    public double getRollingDayHigh() {
        return rollingDayHigh;
    }

    /**
     * Sets a new highest price from the last 24 hours.
     *
     * @param rollingDayHigh the rollingDayHigh to set
     */
    public void setRollingDayHigh(double rollingDayHigh) {
        this.rollingDayHigh = rollingDayHigh;
    }

    /**
     * Returns the day open price.
     *
     * @return the dayOpen
     */
    public double getDayOpen() {
        return dayOpen;
    }

    /**
     * Sets a new day open price.
     *
     * @param dayOpen the dayOpen to set
     */
    public void setDayOpen(double dayOpen) {
        this.dayOpen = dayOpen;
    }

    /**
     * Returns the snapshot time.
     *
     * @return the long
     */
    public final long getTime() {
        return time;
    }
}
