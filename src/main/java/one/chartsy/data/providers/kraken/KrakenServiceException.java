/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.providers.kraken;

/**
 * Informs about the error generated while requesting data from the Kraken api.
 * 
 * @author Mariusz Bernacki
 *
 */
public class KrakenServiceException extends RuntimeException {
    /** The serial version UID. */
    private static final long serialVersionUID = 8458588078376610285L;
    /** The Kraken error code from the API server. */
    private String errorCode;
    /** The human readable Kraken error message returned by the API server. */
    private String message;
    /** The API call which generated the exception. */
    private String apiCall;


    public KrakenServiceException() {
    }

    public KrakenServiceException(KrakenServiceException e) {
        this.errorCode = e.getErrorCode();
        this.message = e.getMessage();
    }

    public static KrakenServiceException fromMessage(String message) {
        String[] parts = message.split(":", 2);
        if (message.startsWith("EAPI:Rate limit exceeded"))
            return new KrakenRateLimitExceededException(parts[1]);
        if (message.startsWith("EService:Unavailable"))
            return new KrakenServiceUnavailableException(parts[1]);

        if (parts.length == 2)
            return new KrakenServiceException(parts[1] + " [" + parts[0] + "]");

        return new KrakenServiceException(message);
    }

    public KrakenServiceException(String message) {
        this.errorCode = message;
        this.message = message;
    }

    public KrakenServiceException(Errors error, String message) {
        this.errorCode = error.name();
        this.message = message;
    }

    public static enum Errors {
        EXT_UNKNOWN_COMMAND,
        EXT_NO_RESULT,
        EXT_INCOMPLETE_RESULT;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gives the API call which generated the exception.
     * 
     * @return the apiCall, may be {@code null}
     */
    public String getApiCall() {
        return apiCall;
    }

    /**
     * Associates an API call with this exception.
     * 
     * @param apiCall the apiCall to set
     */
    public void setApiCall(String apiCall) {
        this.apiCall = apiCall;
    }

    @Override
    public String toString() {
        String result = super.toString();
        String apiCall = getApiCall();
        if (apiCall != null)
            result += "\nFrom API call: " + apiCall;

        return result;
    }
}
