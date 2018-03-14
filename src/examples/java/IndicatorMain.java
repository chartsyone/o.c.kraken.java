import one.chartsy.Quotes;
import one.chartsy.Series;
import one.chartsy.Symbol;
import one.chartsy.TimeFrame;
import one.chartsy.data.providers.kraken.KrakenService;

public class IndicatorMain {

    public static void main(String[] args) throws Exception {
        KrakenService service = KrakenService.getDefault();
        
        // load bitcoin OHLC hourly data
        Quotes quotes = service.getQuotes(new Symbol("XBTUSD"), TimeFrame.Period.H1);
        // calculate ATR(14) indicator
        Series atr = quotes.atr(14);
        System.out.println("Current ATR Value = " + atr.get(0));
    }
}
