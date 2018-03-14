/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.TimeZone;

/**
 * Provides a comprehensive information about a trading symbol. Exposes
 * information such as a name, description (descriptive name), sector, industry,
 * listed exchange, etc.
 * <p>
 * All {@code SymbolInformation} objects are mutable. All exposed properties can
 * be changed and can be {@code null}, except for the {@link #getName() name}
 * property which is non-null and immutable once created. The pair of
 * {@link #getName() name} and {@link #getExchange() exchange} properties
 * uniquely identifies a symbol referenced.
 * 
 * @author Mariusz Bernacki
 *
 */
public class SymbolInformation implements Serializable, Cloneable {
    /** The serial version UID */
    private static final long serialVersionUID = 5805944161288892817L;
    /** The default initial number of display digits used. */
    private static final int DEFAULT_DISPLAY_DIGITS = 8;
    /** The unique identification number of the symbol. */
    private Serializable refId;
    /** The symbol name. */
    private final String name;
    /** The number of significant decimal places displayed. */
    private int displayDigits = DEFAULT_DISPLAY_DIGITS;
    /** The currency in which the symbol is denominated. */
    private CurrencyUnit currency;
    /** The time zone in which all quotes timestamps are reflected. */
    private TimeZone timeZone;

    @ConstructorProperties("name")
    public SymbolInformation(String name) {
        this.name = name;
    }

    static Serializable checkRefId(Serializable refId) {
        if (refId != null) {
            Class<?> type = refId.getClass();
            if (type != String.class && type != Integer.class && type != Long.class)
                throw new IllegalArgumentException(
                        "ID must be either String, Integer or Long; instead found: " + type.getSimpleName());
        }
        return refId;
    }

    @Override
    public SymbolInformation clone() {
        try {
            SymbolInformation result = (SymbolInformation) super.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Shouldn't happen");
        }
    }

    /**
     * Gives the symbol name. The {@code name} together with {@link #getExchange()}
     * uniquely identifies a referred symbol.
     * 
     * @return the symbol name, not-null
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of significant decimal places displayed.
     *
     * @return the displayDigits
     */
    public int getDisplayDigits() {
        return displayDigits;
    }

    /**
     * Sets a new number of significant decimal places displayed.
     *
     * @param displayDigits
     *            the displayDigits to set
     */
    public void setDisplayDigits(int displayDigits) {
        this.displayDigits = displayDigits;
    }

    /**
     * Returns the time zone in which all quotes timestamps are reflected.
     *
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets a new time zone in which all quotes timestamps are reflected.
     *
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Gives the unique identification number of the symbol.
     * 
     * @return the unique id, may be null
     * @see #setRefId(Serializable)
     */
    public Serializable getRefId() {
        return refId;
    }

    /**
     * Sets the unique identification number of the symbol.
     * <p>
     * The identification number is provided and used by data providers for fast
     * lookup of symbols. The {@code id} must be either {@code String},
     * {@code Integer} or {@code Long}; other types aren't currently supported.
     * 
     * @param refId
     *            the unique id to set, may be null
     */
    public void setRefId(Serializable refId) {
        this.refId = checkRefId(refId);
    }

    public CurrencyUnit getCurrency() {
        return currency;
    }

    /**
     * Sets a new currency in which the symbol is denominated.
     *
     * @param currency
     *            the currency to set
     */
    public void setCurrency(CurrencyUnit currency) {
        this.currency = currency;
    }
}
