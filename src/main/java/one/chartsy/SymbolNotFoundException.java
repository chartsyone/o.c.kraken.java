/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

/**
 * Runtime exception thrown when a symbol cannot be found.
 * 
 * @author Mariusz Bernacki
 */
public class SymbolNotFoundException extends RuntimeException {
    /** The serial version UID */
    private static final long serialVersionUID = 7999581764446402397L;

    /**
     * Constructs an instance of this class with the specified message.
     *
     * @param message
     *            the detail message
     */
    public SymbolNotFoundException(String message) {
        super(message);
    }
}
