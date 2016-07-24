package de.mg.stock.server.update;

import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.util.DateTimeProvider;
import de.mg.stock.server.util.HttpUtil;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyzeRetrievedDataTest {

    private StockUpdateFromYahooHistorical histoYahoo;
    private StockUpdateFromGoogleHistorical histoGoogle;

    @Before
    public void setup() {

        histoYahoo = new StockUpdateFromYahooHistorical();
        histoYahoo.setDateTimeProvider(new DateTimeProvider());
        histoYahoo.setHttpUtil(new HttpUtil());

        histoGoogle = new StockUpdateFromGoogleHistorical();
        histoGoogle.setDateTimeProvider(new DateTimeProvider());
        histoGoogle.setHttpUtil(new HttpUtil());
    }

    @Test
    public void compareSymbols() {

        String[] symbols = {"EUNM.DE"};

        List<DayPrice> all = new ArrayList<>();
        for (String symbol : symbols) {
            all.addAll(histoGoogle.get(symbol));
            all.addAll(histoYahoo.get(symbol));
        }

        Map<LocalDate, List<DayPrice>> map = all.stream().collect(Collectors.groupingBy(DayPrice::getDay, Collectors.toList()));

        map.keySet().stream().sorted().forEach(day -> {
            String averages = map.get(day).stream().map(dp -> dp.getAverage().toString()).collect(Collectors.joining(" "));
            System.out.println(day + ": " + averages);
        });

        long maxDiff = 0;
        LocalDate maxDiffDate = null;
        for (LocalDate day : map.keySet()) {
            Long max = map.get(day).stream().map(dp -> dp.getAverage()).max(Long::compareTo).get();
            Long min = map.get(day).stream().map(dp -> dp.getAverage()).min(Long::compareTo).get();
            if (max - min > maxDiff) {
                maxDiff = max - min;
                maxDiffDate = day;
            }
        }
        System.out.println("max difference = " + maxDiff + " " + maxDiffDate);
    }
}
