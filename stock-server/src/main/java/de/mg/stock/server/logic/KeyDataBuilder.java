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

import de.mg.stock.dto.StockKeyDataDto;
import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.Stock;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Singleton
public class KeyDataBuilder {

    private Integer[] DAY_INTERVALS = {3, 7, 30, 90, 180, 365, 2 * 365, 5 * 365, 10 * 365, 20 * 365, 30 * 365};

    @Inject
    private StockDAO stockDAO;

    public List<StockKeyDataDto> create() {

        for (Stock stock : stockDAO.findAllStocks()) {

            StockKeyDataDto data = new StockKeyDataDto();
            data.setStock(StocksEnum.of(stock.getSymbol());

            List<DayPrice> all = stock.getDayPrices();

            for (int dayInterval : DAY_INTERVALS) {

                Optional<DayPrice> min = all.stream().filter(dp -> dp.getDay() == null).min((dp1, dp2) -> dp1.getMin().compareTo(dp2.getMin()));
            }
        }
        return null;
    }

}
