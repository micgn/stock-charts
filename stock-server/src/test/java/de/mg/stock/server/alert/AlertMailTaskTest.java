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
package de.mg.stock.server.alert;


import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.AlertDAO;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.SimpleDayPrice;
import de.mg.stock.server.model.Stock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlertMailTaskTest {

    @InjectMocks
    private AlertMailTask sut;

    @Mock
    private StockDAO stockDAO;
    @Mock
    private AlertDAO alertDAO;
    @Mock
    private AlertMailSender alertMailSender;
    @Mock
    private Stock stock;

    @Captor
    private ArgumentCaptor<Map<StocksEnum, Long>> changePercent;

    private Set<SimpleDayPrice> prices;

    @Before
    public void setup() {

        when(alertDAO.getLastAlertSent()).thenReturn(Optional.empty());

        List<Stock> stocks = new ArrayList<>();
        stocks.add(stock);
        when(stock.getInstantPrices()).thenReturn(
                Arrays.asList(new InstantPrice(LocalDateTime.of(20115, 3, 23, 10, 0, 0), 100l, 100l)));
        prices = new HashSet<>();
        when(stock.getAllPricesDaily()).thenReturn(prices);
        when(stock.getSymbol()).thenReturn(StocksEnum.WORLD.getSymbol());
        when(stockDAO.findAllStocks()).thenReturn(stocks);
    }

    @Test
    public void testNoRelevantChanges() {

        LocalDate today = LocalDate.now();

        prices.add(new SimpleDayPrice(today, 105l));
        prices.add(new SimpleDayPrice(today.minus(1, ChronoUnit.DAYS), 100l));
        prices.add(new SimpleDayPrice(today.minus(2, ChronoUnit.DAYS), 100l));
        prices.add(new SimpleDayPrice(today.minus(3, ChronoUnit.DAYS), 100l));

        sut.checkForAlert();

        verifyNoMoreInteractions(alertMailSender);
    }

    @Test
    public void testRelevantChanges() {

        LocalDate today = LocalDate.now();

        prices.add(new SimpleDayPrice(today, 100l));
        prices.add(new SimpleDayPrice(today.minus(1, ChronoUnit.DAYS), 50l));
        prices.add(new SimpleDayPrice(today.minus(2, ChronoUnit.DAYS), 80l));
        prices.add(new SimpleDayPrice(today.minus(3, ChronoUnit.DAYS), 100l));

        sut.checkForAlert();

        verify(alertMailSender).send(changePercent.capture(), anyString());
        assertThat(changePercent.getValue()).hasSize(1);
        assertThat(changePercent.getValue().get(StocksEnum.WORLD)).isEqualTo(50l);
    }
}
