/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy.data.providers.kraken;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import one.chartsy.CurrencyUnit;
import one.chartsy.Quote;
import one.chartsy.Quotes;
import one.chartsy.Symbol;
import one.chartsy.SymbolInformation;
import one.chartsy.SymbolNotFoundException;
import one.chartsy.TimeFrame;
import one.chartsy.TimeFrameHelper;
import one.chartsy.data.market.Ask;
import one.chartsy.data.market.Bid;
import one.chartsy.data.market.Level1Snapshot;
import one.chartsy.data.market.Level1SnapshotMetaData;
import one.chartsy.data.market.Level2Snapshot;
import one.chartsy.data.market.Trade;
import one.chartsy.time.Chronological;

/**
 * Kraken API client.
 * 
 * @author Mariusz Bernacki
 *
 */
public class KrakenService implements Closeable {
    /** The API url. */
    private static final String API_URL = "https://api.kraken.com/0/";
    /** The http client used for all REST API calls. */
    private final CloseableHttpClient httpClient;
    /** The JSON parser used for unmarshalling responses from the REST API. */
    private final Gson gson = new Gson();
    /** The throttler used to limit the rate of API calls. */
    private final Throttler throttler = new Throttler();


    public KrakenService() {
        this(HttpClientBuilder.create().build());
    }

    private KrakenService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns the underlying HTTP client used for API calls.
     * 
     * @return the HTTP client
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Acquires the HTTP client permit to an API call.
     * 
     * @return the HTTP client
     * @throws IOException
     *             if an API call throttler was sleeping waiting for a time span
     *             allowed for calling the API and at that time an interruption
     *             exception occurred
     */
    protected CloseableHttpClient acquireHttpClientPermit() throws IOException {
        try {
            throttler.acquirePermit();
            return httpClient;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Gives the shared instance of the {@code KrakenService}.
     * 
     * @return the {@code KrakenService} instance
     */
    public static KrakenService getDefault() {
        return Holder.getSharedInstance();
    }

    /**
     * Returns the Kraken's API server current date and time.
     * <p>
     * The method returns date and time in UTC time zone. To convert to your
     * computer's time zone, use the following transformation:
     * 
     * <pre>
     * {@code this.getServerTime().withZoneSameInstant(ZoneId.systemDefault())}
     * </pre>
     * 
     * @return the server date and time (UTC)
     */
    public ZonedDateTime getServerTime() throws IOException, KrakenServiceException {
        return queryPublic(new HttpGet(API_URL + "public/Time"), KRKTimeResult.class,
                KRKTimeResult::getAsDateTime);
    }

    /**
     * Queries the public method of the Kraken API returning parsed and converted
     * result.
     * 
     * @param request
     *            the requested Kraken API method
     * @param jsonType
     *            the type of JSON result to parse the response to
     * @param resultConv
     *            the final method to convert JSON result type to desired custom
     *            type
     * @return the parsed result
     * @throws IOException
     *             if an I/O error occured while executing the method
     */
    protected <R,T> R queryPublic(HttpGet request, Class<T> jsonType, Function<T, R> resultConv) throws IOException {
        try {
            CloseableHttpResponse response = acquireHttpClientPermit().execute(request);
            try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
                return resultConv.apply(gson.fromJson(in, jsonType));
            } finally {
                response.close();
            }
        } catch (KrakenServiceException e) {
            e.setApiCall(request.getURI().getPath());
            throw e;
        }
    }

    private static final Map<String, CurrencyUnit> currencyUnits = Collections.synchronizedMap(new HashMap<>());

    final CurrencyUnit findCurrencyUnit(String apiCode) throws IOException {
        if (currencyUnits.isEmpty()) {
            // Load assets from the API
            Map<String, CurrencyUnit> resultMap = queryPublic(new HttpGet(API_URL + "public/Assets"),
                    KRKAssetResult.class, KRKAssetResult::getAsCurrencyMap);
            synchronized (currencyUnits) {
                if (currencyUnits.isEmpty())
                    currencyUnits.putAll(resultMap);
            }
        }
        CurrencyUnit unit = currencyUnits.get(apiCode);
        if (unit == null)
            throw new KrakenServiceException("Kraken service returned unrecognized currency: " + apiCode);

        return unit;
    }

    /**
     * Returns the URL-encoded comma delimited list of symbols names.
     * 
     * @param symbols
     *            the symbols list
     * @return the encoded symbol names
     * @throws UnsupportedEncodingException
     *             should never happen
     */
    private static String urlEncode(List<Symbol> symbols) throws UnsupportedEncodingException {
        StringBuilder buf = new StringBuilder();
        for (Symbol sym : symbols) {
            if (buf.length() > 0)
                buf.append(',');

            String refId = sym.getRefIdAsString();
            buf.append((refId == null)? sym.getName(): refId);
        }
        return URLEncoder.encode(buf.toString(), "UTF-8");
    }

    /**
     * Returns the URL-encoded symbol name.
     * 
     * @param symbol
     *            the symbol
     * @return the encoded symbol name
     * @throws UnsupportedEncodingException
     *             should never happen
     */
    private static String urlEncode(Symbol symbol) throws UnsupportedEncodingException {
        String refId = symbol.getRefIdAsString();
        return URLEncoder.encode((refId == null)? symbol.getName(): refId, "UTF-8");
    }

    /** Contains the map of tradeable symbols. */
    private static final Map<Serializable, SymbolInformation> symbolInformation = new HashMap<>();

    private void loadSymbolInformation() throws IOException {
        synchronized (KrakenService.class) {
            if (symbolInformation.isEmpty()) {
                List<SymbolInformation> list = queryPublic(new HttpGet(API_URL + "public/AssetPairs"),
                        KRKAssetPairsResult.class, KRKAssetPairsResult::getAsSymbolInformationList);
                for (SymbolInformation info : list)
                    symbolInformation.put(info.getRefId(), info);
            }
        }
    }

    protected final Map<Serializable, SymbolInformation> getSymbolInformation() throws IOException {
        if (symbolInformation.isEmpty())
            loadSymbolInformation();
        return symbolInformation;
    }

    public SymbolInformation getSymbolInformation(Serializable refId) throws IOException {
        SymbolInformation symbolInfo = getSymbolInformation().get(refId);
        if (symbolInfo == null)
            throw new SymbolNotFoundException("Symbol with refId=" + refId + " not found");

        return symbolInfo.clone();
    }

    public Symbol getSymbol(Serializable refId) throws IOException {
        return new Symbol(getSymbolInformation(refId));
    }

    public Level1SnapshotMetaData getLevel1SnapshotMetaData() {
        return capabilities;
    }

    public Level1Snapshot getLevel1Snapshot(Symbol symbol) throws IOException, KrakenServiceException {
        return getLevel1Snapshot(Arrays.asList(symbol)).get(0);
    }

    public List<Level1Snapshot> getLevel1Snapshot(List<Symbol> symbols) throws IOException, KrakenServiceException {
        HttpGet request = new HttpGet(API_URL + "public/Ticker?pair=" + urlEncode(symbols));
        CloseableHttpResponse response = acquireHttpClientPermit().execute(request);
        try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
            long serverTime = 1_000_000 * getServerTimeFromHeader(response);
            return gson.fromJson(in, KRKTickerResult.class).getAsTickerList(serverTime, this);
        } catch (KrakenServiceException e) {
            e.setApiCall(request.getURI().getPath());
            throw e;
        } finally {
            response.close();
        }
    }

    /**
     * Returns the order book (level 2 data) snapshot.
     * 
     * @param symbol
     *            the symbol to get the order book for
     * @return the level 2 data snapshot
     * @throws IOException
     *             if an I/O error occurred while executing the method
     */
    public Level2Snapshot getLevel2Snapshot(Symbol symbol) throws IOException {
        HttpGet request = new HttpGet(API_URL + "public/Depth?pair=" + urlEncode(symbol));
        CloseableHttpResponse response = acquireHttpClientPermit().execute(request);
        try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
            long serverTime = 1_000_000 * getServerTimeFromHeader(response);
            return gson.fromJson(in, KRKOrderBookResult.class).getAsLevel2Data(symbol, serverTime);
        } catch (KrakenServiceException e) {
            e.setApiCall(request.getURI().getPath());
            throw e;
        } finally {
            response.close();
        }
    }

    private static Long removeLastToken(JsonObject jsonObject) {
        if (jsonObject.get("result") != null) {
            JsonObject jsonResultMap = jsonObject.get("result").getAsJsonObject();
            JsonElement last = jsonResultMap.remove("last");
            return last.getAsLong();
        }
        return null;
    }


    private static long getServerTimeFromHeader(HttpResponse response) throws IOException {
        Header header = response.getFirstHeader("Date");
        if (header != null)
            return DateTimeFormatter.RFC_1123_DATE_TIME.parse(header.getValue(), LocalDateTime::from)
                    .toEpochSecond(ZoneOffset.UTC);
        else
            throw new IOException("The \"Date\" header was required but missing in the server response");
    }

    protected Quotes getQuotes(Symbol symbol, TimeFrame period, LocalDateTime from) throws IOException, KrakenServiceException {
        String since = (from == null) ? ""
                : "&since=" + (Chronological.toEpochMicro(from.minusNanos(1)) / 1_000_000L - period.seconds());
        HttpGet request = new HttpGet("https://api.kraken.com/0/public/OHLC?pair=" + urlEncode(symbol)
        + "&interval=" + period.seconds() / 60 + since);

        CloseableHttpResponse response = acquireHttpClientPermit().execute(request);
        try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
            // parse the response into a JSON Object
            JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
            // remove "last" token from the JSON map
            removeLastToken(jsonObject);

            // unmarshall the remaining structure
            int periodShift = period.isIntraday()? period.seconds() : 0;
            List<Quote> coll = gson.fromJson(jsonObject, KRKOHLCResult.class).getAsQuotes(periodShift,
                    getServerTimeFromHeader(response));
            // construct the final result object
            return Quotes.of(symbol, period, coll);
        } catch (KrakenServiceException e) {
            e.setApiCall(request.getURI().toASCIIString());
            throw e;
        } finally {
            response.close();
        }
    }

    public List<Trade> getTradeTicks(Symbol symbol, LocalDateTime from) throws IOException, KrakenServiceException {
        String since = (from == null) ? "" : "&since=" + (Chronological.toEpochMicro(from) * 1000L + from.getNano() % 1000_000); // in-nanos
        HttpGet request = new HttpGet("https://api.kraken.com/0/public/Trades?pair=" + urlEncode(symbol) + since);

        CloseableHttpResponse response = acquireHttpClientPermit().execute(request);
        try (Reader in = new InputStreamReader(response.getEntity().getContent())) {
            // parse the response into a JSON Object
            JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
            // remove "last" token from the JSON map
            removeLastToken(jsonObject);

            // unmarshall the remaining structure
            List<Trade> trades = gson.fromJson(jsonObject, KRKTradesResult.class).getAsTradeList(symbol);
            // construct the final result object
            return trades;
        } catch (KrakenServiceException e) {
            e.setApiCall(request.getURI().toASCIIString());
            throw e;
        } finally {
            response.close();
        }
    }

    static abstract class KRKResultHolder<T> {
        String[] error;
        private final T result;

        KRKResultHolder(T result) {
            this.result = Objects.requireNonNull(result, "result holder is NULL");
        }

        /**
         * Throws an API errors wrapped with a {@code KrakenException}, if a response
         * had errors. Otherwise the method returns uninterrupted.
         * 
         * @throws KrakenServiceException
         *             if an API response had errors
         */
        private void onErrorThrowEx() throws KrakenServiceException {
            String[] errors = this.error;
            if (errors != null && errors.length > 0) {
                KrakenServiceException e = KrakenServiceException.fromMessage(errors[0]);
                for (int i = 1; i < errors.length; i++)
                    e.addSuppressed(KrakenServiceException.fromMessage(errors[i]));

                throw e;
            }
        }

        /**
         * Returns the response result stored in this holder.
         * 
         * @return the response result
         */
        T getResult() {
            onErrorThrowEx();
            if (result == null)
                throw new KrakenServiceException("Server returned invalid response: No `result` in JSON map");

            return result;
        }
    }

    static class KRKOHLCResult extends KRKResultHolder<Map<String, double[][]>> {
        KRKOHLCResult() {
            super(new TreeMap<String, double[][]>());
        }

        List<Quote> getAsQuotes(int periodShift, long serverTime) {
            Map<String, double[][]> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response: No `result` in JSON map");

            double[][] rows = result.values().iterator().next();
            List<Quote> resultList = new ArrayList<>(rows.length);
            for (double[] row : rows) {
                long timestamp = 1_000_000L*(Math.min((long)row[0] + periodShift, serverTime));
                Quote quote = new Quote(timestamp, row[1], row[2], row[3], row[4], row[6]);
                resultList.add(quote);
            }
            return resultList;
        }
    }

    static class KRKTradesResult extends KRKResultHolder<Map<String, String[][]>> {
        KRKTradesResult() {
            super(new TreeMap<>());
        }

        List<Trade> getAsTradeList(Symbol symbol) {
            Map<String, String[][]> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response (empty result map in JSON result)");

            String[][] rows = result.values().iterator().next();
            List<Trade> resultList = new ArrayList<>(rows.length);
            for (String[] row : rows) {
                double price = Double.parseDouble(row[0]);
                double volume = Double.parseDouble(row[1]);
                double epochSeconds = Double.parseDouble(row[2]);
                Trade.Direction direction = Trade.Direction.parse(row[3], "b", "s");

                long epochMicros = (long) (1_000_000.0 * epochSeconds);
                resultList.add(new Trade(symbol, epochMicros, price, volume, direction));
            }
            return resultList;
        }
    }

    static class KRKOrderBookResult extends KRKResultHolder<Map<String, KRKOrderBook>> {
        KRKOrderBookResult() {
            super(new TreeMap<>());
        }

        Level2Snapshot getAsLevel2Data(Symbol symbol, long serverTime) {
            Map<String, KRKOrderBook> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response (empty result map in JSON result)");
            if (result.size() != 1)
                throw new KrakenServiceException("Server returned invalid response. Too many entries in JSON result map: " + result.keySet());

            KRKOrderBook rows = result.values().iterator().next();
            return rows.getAsLevel2Data(symbol, serverTime);
        }
    }

    static class KRKOrderBook {
        double[][] asks;
        double[][] bids;

        Level2Snapshot getAsLevel2Data(Symbol symbol, long serverTime) {
            if (asks == null)
                throw new KrakenServiceException("Server returned invalid response (result list `asks` is empty)");
            if (bids == null)
                throw new KrakenServiceException("Server returned invalid response (result list `bids` is empty)");

            List<Ask> a = new ArrayList<>(asks.length);
            for (int i = 0; i < asks.length; i++)
                a.add(new Ask(symbol, Math.round(asks[i][2] * 1_000_000.), asks[i][0], asks[i][1]));
            List<Bid> b = new ArrayList<>(bids.length);
            for (int i = 0; i < bids.length; i++)
                b.add(new Bid(symbol, Math.round(bids[i][2] * 1_000_000.), bids[i][0], bids[i][1]));

            return new Level2Snapshot(symbol, serverTime, b, a);
        }
    }

    public static class Throttler {
        /** The maximum number of requests in a period. */
        private int rateLimit = 10;

        private Duration period = Duration.ofSeconds(35);

        private final LinkedList<LocalDateTime> requestQueue = new LinkedList<>();

        public Throttler() {
        }

        public Throttler(int rateLimit, Duration period) {
            if (rateLimit < 1 || rateLimit > 999)
                throw new IllegalArgumentException("The `rateLimit` argument must be in range 1-999");
            long millis = period.toMillis();
            if (millis < 1 || millis > 59_000)
                throw new IllegalArgumentException("The `period` argument must be in range 1-59 seconds");
            this.period = period;
        }

        public synchronized void acquirePermit() throws InterruptedException {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime head = now.minus(period);
            Iterator<LocalDateTime> iter = requestQueue.iterator();
            while (iter.hasNext()) {
                if (iter.next().isAfter(head))
                    break;
                iter.remove();
            }

            if (requestQueue.size() >= rateLimit) {
                if (SwingUtilities.isEventDispatchThread())
                    throw new KrakenRateLimitExceededException("Cannot throttle in an Event Thread");
                long waitMillis = head.until(requestQueue.get(requestQueue.size() - rateLimit), ChronoUnit.MILLIS);
                if (waitMillis > 0)
                    Thread.sleep(waitMillis);
            }
            requestQueue.add(now);
        }
    }

    static class IBKRTickerResult {
        @SerializedName("EUR.USD") IBKRTicker eurUsd;
        String updatetime;
    }

    static class IBKRTicker {
        String symbol;
        String bid, ask;

        String bid() {
            return this.bid.replace("<sup>", "").replace("</sup>", "");
        }

        String ask() {
            return this.ask.replace("<sup>", "").replace("</sup>", "");
        }

        @Override
        public String toString() {
            String bid = this.bid.replace("<sup>", "").replace("</sup>", "");
            String ask = this.ask.replace("<sup>", "").replace("</sup>", "");
            return symbol + " B: " + bid + ", A: "+ ask;
        }
    }

    static class KRKTickerResult extends KRKResultHolder<Map<String, KRKTicker>> {
        KRKTickerResult() {
            super(new TreeMap<>());
        }

        List<Level1Snapshot> getAsTickerList(long serverTime, KrakenService provider) throws IOException {
            Map<String, KRKTicker> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response: No `result` in JSON map");

            List<Level1Snapshot> resultList = new ArrayList<>();
            for (Entry<String, KRKTicker> e : result.entrySet()) {
                Level1Snapshot ticker = new Level1Snapshot(provider.getSymbol(e.getKey()), serverTime);
                resultList.add(e.getValue().getAsTicker(ticker));
            }
            return resultList;
        }
    }

    /** Holds a ticker info for a tradeable asset pair. */
    static class KRKTicker {
        /** The ask array (price, whole lot volume, lot volume). */
        double[] a;
        /** The bid array (price, whole lot volume, lot volume). */
        double[] b;
        /** The last trade closed array(price, lot volume). */
        double[] c;
        /** The volume array(today, last 24 hours). */
        double[] v;
        /** The volume weighted average price array(today, last 24 hours). */
        double[] p;
        /** The number of trades array(today, last 24 hours). */
        long[] t;
        /** The low array(today, last 24 hours). */
        double[] l;
        /** The high array(today, last 24 hours). */
        double[] h;
        /** The today's opening price. */
        double o;

        Level1Snapshot getAsTicker(Level1Snapshot r) {
            r.setAsk(new Ask(r.getSymbol(), r.getTime(), a[0], 0.));
            r.setBid(new Bid(r.getSymbol(), r.getTime(), b[0], 0.));
            r.setLastTrade(new Trade(r.getSymbol(), r.getTime(), c[0], c[1], Trade.Direction.UNKNOWN));
            r.setDayVolume(v[0]);
            r.setRollingDayVolume(v[1]);
            r.setDayVwap(p[0]);
            r.setRollingDayVwap(p[1]);
            r.setDayTradeNumber(t[0]);
            r.setRollingDayTradeNumber(t[1]);
            r.setDayLow(l[0]);
            r.setRollingDayLow(l[1]);
            r.setDayHigh(h[0]);
            r.setRollingDayHigh(h[1]);
            r.setDayOpen(o);

            return r;
        }
    }

    static class KRKAssetPairsResult extends KRKResultHolder<Map<String, KRKAssetPair>> {
        KRKAssetPairsResult() {
            super(new HashMap<>());
        }

        List<SymbolInformation> getAsSymbolInformationList() {
            Map<String, KRKAssetPair> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response: No `result` in JSON map");

            List<SymbolInformation> resultMap = new ArrayList<>(result.size());
            for (Entry<String, KRKAssetPair> e : result.entrySet()) {
                SymbolInformation si = new SymbolInformation(e.getValue().altname);
                si.setRefId(e.getKey());
                resultMap.add(si);
            }
            return resultMap;
        }
    }

    static class KRKAssetResult extends KRKResultHolder<Map<String, KRKAsset>> {
        KRKAssetResult() {
            super(new HashMap<>());
        }

        Map<String, CurrencyUnit> getAsCurrencyMap() {
            Map<String, KRKAsset> result = getResult();
            if (result.isEmpty())
                throw new KrakenServiceException("Server returned invalid response: No `result` in JSON map");

            Map<String, CurrencyUnit> resultMap = new HashMap<>(result.size());
            for (Entry<String, KRKAsset> e : result.entrySet()) {
                KRKAsset asset = e.getValue();
                String assetName = asset.altname;
                if ("XBT".equals(assetName))
                    assetName = "BTC";
                if ("XDG".equals(assetName))
                    assetName = "DOGE";

                resultMap.put(e.getKey(), CurrencyUnit.create(assetName, asset.display_decimals));
            }
            return resultMap;
        }
    }

    /**
     * {"altname":"BCHEUR","aclass_base":"currency","base":"BCH","aclass_quote":"currency","quote":"ZEUR","lot":"unit","pair_decimals":1,
     * "lot_decimals":8,"lot_multiplier":1,"leverage_buy":[],"leverage_sell":[],
     * "fees":[[0,0.26],[50000,0.24],[100000,0.22],[250000,0.2],[500000,0.18],[1000000,0.16],[2500000,0.14],[5000000,0.12],[10000000,0.1]],
     * "fees_maker":[[0,0.16],[50000,0.14],[100000,0.12],[250000,0.1],[500000,0.08],[1000000,0.06],[2500000,0.04],[5000000,0.02],[10000000,0]],
     * "fee_volume_currency":"ZUSD","margin_call":80,"margin_stop":40}
     *
     */
    static class KRKAssetPair {
        String altname;
        String aclass_base;
        String base;
        String aclass_quote;
        String quote;
        String lot;
        Integer pair_decimals;
        Integer lot_decimals;
        Integer lot_multiplier;


        @Override
        public String toString() {
            return ""+pair_decimals;
        }
    }

    private static class KRKAsset {
        String altname;
        Integer display_decimals;

    }

    /** The ServerTime JSON result object. */
    static class KRKTime {
        Long unixtime;
    }

    /** The ServerTime response result holder. */
    static class KRKTimeResult extends KRKResultHolder<KRKTime> {
        KRKTimeResult() {
            super(new KRKTime());
        }

        ZonedDateTime getAsDateTime() {
            KRKTime result = getResult();
            if (result.unixtime == null)
                throw new KrakenServiceException("Server returned incomplete result; 'unixtime' is required but missing.");

            return LocalDateTime.ofEpochSecond(result.unixtime, 0, ZoneOffset.UTC).atZone(ZoneOffset.UTC);
        }
    }

    private static final List<TimeFrame.Period> availableTimeFrames = Arrays.asList(
            TimeFrame.Period.WEEKLY,
            TimeFrame.Period.DAILY,
            TimeFrame.Period.H4,
            TimeFrame.Period.H1,
            TimeFrame.Period.M30,
            TimeFrame.Period.M15,
            TimeFrame.Period.M5,
            TimeFrame.Period.M1
    );

    public Quotes getQuotes(Symbol symbol, TimeFrame period) throws IOException {
        return getQuotes(symbol, period, null, null);
    }
    
    public Quotes getQuotes(Symbol symbol, TimeFrame period, LocalDateTime from, LocalDateTime to) throws IOException {
        return getQuotes(symbol, period, from, to, null);
    }

    public Quotes getQuotes(Symbol symbol, TimeFrame period, LocalDateTime from, LocalDateTime to, Integer maxBars)
            throws IOException {
        for (TimeFrame.Period timeFrame : availableTimeFrames)
            if (period.isAssignableFrom(timeFrame)) {
                Quotes quotes = getQuotes(symbol, timeFrame, from);

                if (!period.equals(timeFrame))
                    quotes = TimeFrameHelper.timeFrameCompress(period, quotes);
                if (maxBars != null)
                    quotes = quotes.trimToLength(maxBars);
                return quotes;
            }
        throw new KrakenServiceException("Unsupported Time Frame: " + period);
    }

    public TimeFrame getBaseTimeFrame(Symbol symbol) {
        return TimeFrame.Period.M1;
    }

    private static final class Holder {
        private static WeakReference<KrakenService> INSTANCE = new WeakReference<>(null);
        private static ReferenceQueue<KrakenService> REF_QUEUE = new ReferenceQueue<>();

        static synchronized KrakenService getSharedInstance() {
            KrakenService instance = INSTANCE.get();
            if (instance == null) {
                INSTANCE = new ServiceRef(instance = new KrakenService(), REF_QUEUE);
                new GCFinalizer().start();
            }
            return instance;
        }

        private static final class ServiceRef extends WeakReference<KrakenService> {
            private final CloseableHttpClient httpClient;

            public ServiceRef(KrakenService r, ReferenceQueue<? super KrakenService> q) {
                super(r, q);
                this.httpClient = r.httpClient;
            }

            public void dispose() {
                try {
                    Closeable c = httpClient;
                    if (c != null)
                        c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static final class GCFinalizer extends Thread {
            public GCFinalizer() {
                setName("KrakenService Finalizer");
                setDaemon(true);
            }

            @Override
            public void run() {
                try {
                    ((ServiceRef) REF_QUEUE.remove()).dispose();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        // do not close the default service
        if (Holder.INSTANCE.get() != this)
            httpClient.close();
    }

    /** The service capabilities. */
    private Capabilities capabilities = new Capabilities();

    private static final class Capabilities implements Level1SnapshotMetaData {
        @Override
        public boolean isQuoteTickPriceSupported() {
            return true;
        }

        @Override
        public boolean isQuoteTickVolumeSupported() {
            return false;
        }

        @Override
        public boolean isLastPriceSupported() {
            return true;
        }

        @Override
        public boolean isLastVolumeSupported() {
            return true;
        }

        @Override
        public boolean isDayVolumeSupported() {
            return true;
        }

        @Override
        public boolean isRollingDayVolumeSupported() {
            return true;
        }

        @Override
        public boolean isDayVwapSupported() {
            return true;
        }

        @Override
        public boolean isRollingDayVwapSupported() {
            return true;
        }

        @Override
        public boolean isDayTradeNumberSupported() {
            return true;
        }

        @Override
        public boolean isRollingDayTradeNumberSupported() {
            return true;
        }

        @Override
        public boolean isDayLowSupported() {
            return true;
        }

        @Override
        public boolean isRollingDayLowSupported() {
            return true;
        }

        @Override
        public boolean isDayHighSupported() {
            return true;
        }

        @Override
        public boolean isRollingDayHighSupported() {
            return true;
        }

        @Override
        public boolean isDayOpenSupported() {
            return true;
        }
    }
}
