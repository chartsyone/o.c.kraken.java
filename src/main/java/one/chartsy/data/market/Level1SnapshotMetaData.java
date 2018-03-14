/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.market;

/**
 * Provides information what Level 1 snapshot data is available.
 * 
 * @author Mariusz Bernacki
 *
 */
public interface Level1SnapshotMetaData {

    /**
     * Returns whether the bid and ask price is supported.
     *
     * @return the quoteTickPriceSupported
     */
    boolean isQuoteTickPriceSupported();

    /**
     * Returns whether the bid and ask volume is supported.
     *
     * @return the quoteTickVolumeSupported
     */
    boolean isQuoteTickVolumeSupported();

    /**
     * Returns whether the last trade price is supported.
     *
     * @return the lastPriceSupported
     */
    boolean isLastPriceSupported();

    /**
     * Returns whether the volume of the last trade is supported.
     *
     * @return the lastVolumeSupported
     */
    boolean isLastVolumeSupported();

    /**
     * Returns whether the current day volume is supported.
     *
     * @return the dayVolumeSupported
     */
    boolean isDayVolumeSupported();

    /**
     * Returns whether the total volume from the last 24 hours is supported.
     *
     * @return the rollingDayVolumeSupported
     */
    boolean isRollingDayVolumeSupported();

    /**
     * Returns whether the current day's volume weighted average price is supported.
     *
     * @return the dayVwapSupported
     */
    boolean isDayVwapSupported();

    /**
     * Returns whether the volume weighted average price of the last 24 hours is supported.
     *
     * @return the rollingDayVwapSupported
     */
    boolean isRollingDayVwapSupported();

    /**
     * Returns whether the number of trades in the current day is supported.
     *
     * @return the dayTradeNumberSupported
     */
    boolean isDayTradeNumberSupported();

    /**
     * Returns whether the number of trades from the last 24 hours is supported.
     *
     * @return the rollingDayTradeNumberSupported
     */
    boolean isRollingDayTradeNumberSupported();

    /**
     * Returns whether the lowest price of the day is supported.
     *
     * @return the dayLowSupported
     */
    boolean isDayLowSupported();

    /**
     * Returns whether the lowest price from the last 24 hours is supported.
     *
     * @return the rollingDayLowSupported
     */
    boolean isRollingDayLowSupported();

    /**
     * Returns whether the highest price of the day is supported.
     *
     * @return the dayHighSupported
     */
    boolean isDayHighSupported();

    /**
     * Returns whether the highest price from the last 24 hours is supported.
     *
     * @return the rollingDayHighSupported
     */
    boolean isRollingDayHighSupported();

    /**
     * Returns whether the day open price is supported.
     *
     * @return the dayOpenSupported
     */
    boolean isDayOpenSupported();

}
