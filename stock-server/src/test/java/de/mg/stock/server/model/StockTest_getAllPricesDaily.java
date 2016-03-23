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
package de.mg.stock.server.model;


import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static java.time.LocalDate.of;
import static org.assertj.core.api.Assertions.assertThat;

public class StockTest_getAllPricesDaily {

    Stock sut;

    @Before
    public void setup() {
        sut = new Stock("symbol");

        sut.getDayPrices().add(new DayPrice(of(2016, 3, 23), 50l, 150l));
        sut.getDayPrices().add(new DayPrice(of(2016, 3, 24), 50l, 150l));
        sut.getDayPrices().add(new DayPrice(of(2016, 3, 25), 50l, 150l));

        sut.getInstantPrices().add(new InstantPrice(LocalDateTime.of(2016, 3, 25, 10, 0, 0), 100l, 300l));
        sut.getInstantPrices().add(new InstantPrice(LocalDateTime.of(2016, 3, 25, 10, 0, 0), 150l, 150l));
        sut.getInstantPrices().add(new InstantPrice(LocalDateTime.of(2016, 3, 26, 12, 0, 0), 50l, 150l));
    }

    @Test
    public void test() {

        Set<SimpleDayPrice> result = sut.getAllPricesDaily();
        assertThat(result).hasSize(4);
        assertThat(result).contains(new SimpleDayPrice(of(2016, 3, 23), 100l));
        assertThat(result).contains(new SimpleDayPrice(of(2016, 3, 24), 100l));
        assertThat(result).contains(new SimpleDayPrice(of(2016, 3, 25), 150l));
        assertThat(result).contains(new SimpleDayPrice(of(2016, 3, 26), 100l));
    }
}
