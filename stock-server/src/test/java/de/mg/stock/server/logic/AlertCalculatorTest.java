package de.mg.stock.server.logic;


import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.Stock;
import de.mg.stock.server.util.DateTimeProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AlertCalculatorTest {

    @Mock
    private StockDAO stockDAO;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private final AlertCalculator sut = new AlertCalculator();

    @Before
    public void setup() {

        when(dateTimeProvider.today()).thenReturn(LocalDate.of(2016, 1, 5));
    }


    @Test
    public void testNoChange() {

        Stock s = new Stock(StocksEnum.WORLD.getSymbol());
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 1000L, 1000L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 2), 1000L, 1000L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 1000L, 1000L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 4), 1000L, 1000L));
        s.getInstantPrices().add(new InstantPrice(LocalDateTime.of(2016, 1, 4, 10, 00), 1000L, 1000L));

        when(stockDAO.findAllStocks()).thenReturn(Arrays.asList(s));
        Map<StocksEnum, Long> changes = sut.weeklyChanges();
        assertThat(changes).isEmpty();
    }
    @Test
    public void testSomeChange() {

        Stock s = new Stock(StocksEnum.WORLD.getSymbol());
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 1000L, 1000L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 2), 1000L, 1000L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 1500L, 1500L));
        s.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 4), 1000L, 1000L));
        s.getInstantPrices().add(new InstantPrice(LocalDateTime.of(2016, 1, 4, 10, 00), 1000L, 1000L));

        when(stockDAO.findAllStocks()).thenReturn(Arrays.asList(s));
        Map<StocksEnum, Long> changes = sut.weeklyChanges();
        assertThat(changes).hasSize(1);
        assertThat(changes).containsValues(-50L);
    }
}
