/*
 * Copyright 2016 Michael Gnatz.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.mg.stock.server.logic;

import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.Stock;
import de.mg.stock.server.util.DateTimeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ChartBuilderTest {

    @Mock
    DateTimeProvider dateTimeProvider;

    @InjectMocks
    ChartBuilder sut = new ChartBuilder();

    @Test
    public void test() {

        Stock stock = new Stock();
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 5), 3L, 3L));
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 1), 1L, 5L));
        stock.getDayPrices().add(new DayPrice(LocalDate.of(2016, 1, 3), 2L, 3L));
        ChartDataDTO dto = sut.createOne(stock, 2, Optional.empty(), false);
        System.out.println(dto);
    }

    @Test
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

        ChartDataDTO dto = sut.createAggregated(stocks, weights, 10, Optional.empty());
        System.out.println(dto);
    }
}
