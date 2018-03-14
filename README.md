# Java API client for the Kraken cryptocurrency exchange: kraken.com.

This library unmarshalls JSON responses into the Java objects (e.g. Trade, Quote,...).
A wide collection of Java support objects is provided in this library, designed to be API-agnostic and preferably support additional exchanges in the future.

This project is an early work in progress. Once completed it's planned to be integrated with the [Chartsy|One](https://www.chartsy.one) platform.
Currently only public API methods are supported.

## Roadmap
- Publish unit and integration tests.
- Implement Private API methods.

## Project dependencies

- GSON
- Apache Http Components Client

## Example usage
### 1. Basics
Kraken API methods are available through the KrakenService object, which can be instantiated using a static factory method:

```java
    KrakenService service = KrakenService.getInstance();
```

### 2. Loading hourly OHLC data:

```java
    Quotes quotes = service.getQuotes(new Symbol("XBTUSD"), TimeFrame.Period.H1);
    System.out.println("Number of Bars = " + quotes.length());
    System.out.println("First Bar = " + quotes.getFirst());
    System.out.println("Last Bar = " + quotes.get(0));
```

The library supports a basic set of technical and statistical indicators. For example the following code loads hourly OHLC data and calculates the 14-period Average True Range of the result:

```java
    Quotes quotes = service.getQuotes(new Symbol("XBTUSD"), TimeFrame.Period.H1);
    Series atr = quotes.atr(14);
    System.out.println("Current ATR Value = " + atr.get(0));
```

### 3. Downloading all historical ticks to a CSV file:

```java
    KrakenService service = KrakenService.getDefault();

    DecimalFormat df = new DecimalFormat("0.########");
    BufferedWriter out = new BufferedWriter(new FileWriter(FILENAME));
    LocalDateTime startDate = Chronological.toDateTime(0);
    try {
        int k = 0;
        while (true) {
            try {
                List<Trade> ticks = service.getTradeTicks(new Symbol("XBTUSD"), startDate);
                if (ticks.size() == 0)
                    break;
                System.out.println("TICK PACK OF " + ticks.size() + " TICKS LOADED:");
                System.out.println(ticks.get(0));
                System.out.println(ticks.get(1));
                System.out.println(ticks.get(2));
                System.out.println(ticks.get(ticks.size()-1));
                System.out.println(ticks.size());
                System.out.println("LOOP COUNT " + ++k);

                for (Trade tick : ticks)
                    out.write(tick.getSymbol() + ";" + tick.getDateTime().toString().replace('T', ';') + ";" + tick.getPrice() + ";" + df.format(tick.getVolume()).replace(',', '.') + ";" + ((tick.getDirection() == Direction.BUY_ASK)? "L" : (tick.getDirection() == Direction.SELL_BID)? "S" : "") + "\r\n");
                out.flush();
                startDate = ticks.get(ticks.size()-1).getDateTime();
            } catch (KrakenServiceUnavailableException e) {
                System.out.println(e);
                Thread.sleep(2L);
            }
        }
    } finally {
        out.close();
    }
```