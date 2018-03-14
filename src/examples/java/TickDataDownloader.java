import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import one.chartsy.Symbol;
import one.chartsy.data.market.Trade;
import one.chartsy.data.market.Trade.Direction;
import one.chartsy.data.providers.kraken.KrakenService;
import one.chartsy.data.providers.kraken.KrakenServiceUnavailableException;
import one.chartsy.time.Chronological;

public class TickDataDownloader {
    
    static final String FILENAME = "D:/Downloads/Windows/KRAKEN_BTCUSD_20.csv";

    public static void main(String[] args) throws ClientProtocolException, IOException, ParseException, InterruptedException {
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
    }
}
