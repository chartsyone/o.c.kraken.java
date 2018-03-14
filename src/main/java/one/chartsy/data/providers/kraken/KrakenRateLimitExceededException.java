/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.providers.kraken;

/**
 * Informs about API rate limits exceeded.
 * 
 * @author Mariusz Bernacki
 *
 */
public class KrakenRateLimitExceededException extends KrakenServiceException {
    private static final long serialVersionUID = 2822126989331078418L;

    public KrakenRateLimitExceededException(String message) {
        super(message);
    }
}
