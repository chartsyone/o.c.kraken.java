import one.chartsy.Quotes;
import one.chartsy.Symbol;
import one.chartsy.TimeFrame;
import one.chartsy.data.providers.kraken.KrakenService;

public class HourlyDataMain {

    public static void main(String[] args) throws Exception {
        KrakenService service = KrakenService.getDefault();
        
        // load bitcoin OHLC hourly data
        Quotes quotes = service.getQuotes(new Symbol("XBTUSD"), TimeFrame.Period.H1);
        System.out.println("Number of Bars = " + quotes.length());
        System.out.println("First Bar = " + quotes.getFirst());
        System.out.println("Last Bar = " + quotes.get(0));
    }
}
