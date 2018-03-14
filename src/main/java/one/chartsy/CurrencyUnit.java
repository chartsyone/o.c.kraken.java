/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.util.HashMap;
import java.util.Map;

/**
 * A unit of currency, such as US Dollar, Euro, Bitcoin or other.
 * <p>
 * All currencies are distinguished by unique {@link #getCurrencyCode()}.
 * 
 * @author Mariusz Bernacki
 *
 */
public final class CurrencyUnit implements java.io.Serializable {
    /** The serial version UID. */
    private static final long serialVersionUID = 4598061639098872592L;
    /** The predefined collection of default currency units. */
    private static final Map<String, CurrencyUnit> sharedCurrencyUnits = new HashMap<>();
    /** The Australian Dollar. */
    public static final CurrencyUnit AUD = new CurrencyUnit(36, "AUD", 2);
    /** The Brazilian Real. */
    public static final CurrencyUnit BRL = new CurrencyUnit(986, "BRL", 2);
    /** The Bitcoin cryptocurrency. */
    public static final CurrencyUnit BTC = new CurrencyUnit(-1, "BTC", 8);
    /** The Canadian Dollar. */
    public static final CurrencyUnit CAD = new CurrencyUnit(124, "CAD", 2);
    /** The Swiss Franc. */
    public static final CurrencyUnit CHF = new CurrencyUnit(756, "CHF", 2);
    /** The Chinese Yuan. */
    public static final CurrencyUnit CNY = new CurrencyUnit(156, "CNY", 2);
    /** The Czech Republic Koruna. */
    public static final CurrencyUnit CZK = new CurrencyUnit(203, "CZK", 2);
    /** The Danish Krone. */
    public static final CurrencyUnit DKK = new CurrencyUnit(208, "DKK", 2);
    /** The Ethereum cryptocurrency. */
    public static final CurrencyUnit ETH = new CurrencyUnit(-1, "ETH", 8);
    /** The Euro. */
    public static final CurrencyUnit EUR = new CurrencyUnit(978, "EUR", 2);
    /** The British Pound Sterling. */
    public static final CurrencyUnit GBP = new CurrencyUnit(826, "GBP", 2);
    /** The Hong Kong Dollar. */
    public static final CurrencyUnit HKD = new CurrencyUnit(344, "HKD", 2);
    /** The Hungarian Forint. */
    public static final CurrencyUnit HUF = new CurrencyUnit(348, "HUF", 2);
    /** The Israeli New Sheqel. */
    public static final CurrencyUnit ILS = new CurrencyUnit(376, "ILS", 2);
    /** The Indian Rupee. */
    public static final CurrencyUnit INR = new CurrencyUnit(356, "INR", 2);
    /** The Japanese Yen. */
    public static final CurrencyUnit JPY = new CurrencyUnit(392, "JPY", 0);
    /** The South Korean Won. */
    public static final CurrencyUnit KRW = new CurrencyUnit(410, "KRW", 0);
    /** The Mexican Peso. */
    public static final CurrencyUnit MXN = new CurrencyUnit(484, "MXN", 2);
    /** The Malaysian Ringgit. */
    public static final CurrencyUnit MYR = new CurrencyUnit(458, "MYR", 2);
    /** The Norwegian Krone. */
    public static final CurrencyUnit NOK = new CurrencyUnit(578, "NOK", 2);
    /** The New Zealand Dollar. */
    public static final CurrencyUnit NZD = new CurrencyUnit(554, "NZD", 2);
    /** The Polish Zloty. */
    public static final CurrencyUnit PLN = new CurrencyUnit(985, "PLN", 2);
    /** The Russian Ruble. */
    public static final CurrencyUnit RUB = new CurrencyUnit(643, "RUB", 2);
    /** The Swedish Krona. */
    public static final CurrencyUnit SEK = new CurrencyUnit(752, "SEK", 2);
    /** The Singapore Dollar. */
    public static final CurrencyUnit SGD = new CurrencyUnit(702, "SGD", 2);
    /** The Thai Baht. */
    public static final CurrencyUnit THB = new CurrencyUnit(764, "THB", 2);
    /** The Turkish Lira. */
    public static final CurrencyUnit TRY = new CurrencyUnit(949, "TRY", 2);
    /** The New Taiwan Dollar. */
    public static final CurrencyUnit TWD = new CurrencyUnit(901, "TWD", 2);
    /** The US Dollar. */
    public static final CurrencyUnit USD = new CurrencyUnit(840, "USD", 2);
    /** The Ripple cryptocurrency. */
    public static final CurrencyUnit XRP = new CurrencyUnit(-1, "XRP", 6);
    /** The South African Rand. */
    public static final CurrencyUnit ZAR = new CurrencyUnit(710, "ZAR", 2);

    /** The unique currency code. */
    private final String currencyCode;
    /** The currency numeric code. */
    private final int numericCode;
    /** The default number of fraction digits. */
    private final int defaultFractionDigits;


    private CurrencyUnit(int numericCode, String currencyCode, int defaultFractionDigits) {
        this(currencyCode, numericCode, defaultFractionDigits);
        sharedCurrencyUnits.put(currencyCode, this);
    }

    private CurrencyUnit(String currencyCode, int numericCode, int defaultFractionDigits) {
        this.currencyCode = currencyCode;
        this.numericCode = numericCode;
        this.defaultFractionDigits = defaultFractionDigits;
    }

    public static CurrencyUnit of(String currencyCode) {
        CurrencyUnit unit = sharedCurrencyUnits.get(currencyCode);
        if (unit == null)
            throw new IllegalArgumentException("Unrecognized currency \"" + currencyCode + "\"");

        return unit;
    }

    public static CurrencyUnit create(String currencyCode, int defaultFractionDigits) {
        CurrencyUnit unit = sharedCurrencyUnits.get(currencyCode);
        if (unit == null)
            unit = new CurrencyUnit(currencyCode, -1, defaultFractionDigits);

        return unit;
    }

    @Override
    public int hashCode() {
        return currencyCode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CurrencyUnit)
            return ((CurrencyUnit) obj).currencyCode.equals(currencyCode);
        return false;
    }

    /**
     * Gives the unique currency code.
     * 
     * @return
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public int getDefaultFractionDigits() {
        return defaultFractionDigits;
    }

    @Override
    public String toString() {
        return currencyCode;
    }

    /**
     * Handles proper deserialization of singleton (default) instances.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        CurrencyUnit unit = sharedCurrencyUnits.get(currencyCode);
        return (unit == null)? this : unit;
    }
}
