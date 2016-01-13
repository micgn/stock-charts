package de.mg.stock.server;

import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.Stock;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChartBuilderTest {

    ChartBuilder builder = new ChartBuilder();

    //@Test
    public void test() {

        Stock stock = new Stock();
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 5), 3L, 3L));
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 1L, 5L));
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 2L, 3L));
        ChartDataDTO dto = builder.createAggregated(stock, 2, null, false);
        System.out.println(dto);
    }

    //@Test
    public void test2() {

        Stock s1 = new Stock();
        s1.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 100L, 100L));
        s1.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 120L, 120L));
        s1.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 5), 150L, 150L));

        Stock s2 = new Stock();
        s2.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 100L, 100L));
        s2.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 130L, 150L));
        s2.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 5), 150L, 150L));

        List<Stock> stocks = new ArrayList<>();
        stocks.add(s1);
        stocks.add(s2);

        List<Integer> weights = new ArrayList<>();
        weights.add(50);
        weights.add(50);

        ChartDataDTO dto = builder.createAggregated(stocks, weights, 10, Optional.empty());
        System.out.println(dto);
    }
}
