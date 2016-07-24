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

package de.mg.stock.server.update;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.Stock;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

import static de.mg.stock.dto.StocksEnum.*;
import static javax.ejb.TransactionAttributeType.REQUIRED;

@Stateless
@TransactionAttribute(REQUIRED)
public class StockUpdateTasks {

    private static Logger logger = Logger.getLogger(StockUpdateTasks.class.getName());

    @Inject
    private StockUpdateFromYahooHistorical yahoo;

    @Inject
    private StockUpdateFromGoogleHistorical google;

    @Inject
    private StockUpdateFromYahooInstant yahooInstant;

    @Inject
    private StockDAO stockDAO;

    @Schedule(hour = "5, 22", persistent = false)
    public void updateHistoricalData() {
        updateHistoricalData(EMERGING, google);
        updateHistoricalData(WORLD, yahoo);
        updateHistoricalData(SMALL200, yahoo);
    }

    @Schedule(hour = "7-23", minute = "5, 15, 25, 35, 45, 55", persistent = false)
    public void updateInstantData() {
        updateInstantData(WORLD);
        updateInstantData(SMALL200);
        updateInstantData(EMERGING);
    }

    @Schedule(hour = "4", persistent = false)
    public void cleanup() {
        stockDAO.deleteOldInstantData();
    }


    @Asynchronous
    public void updateAsync() {
        stockDAO.deleteOldInstantData();
        updateInstantData();
        updateHistoricalData();
    }

    private void updateHistoricalData(StocksEnum stocksEnum, StockUpdaterHistorical updater) {
        String symbol = stocksEnum.getSymbol();

        logger.info("going for historical data: " + symbol + " (" + updater.getClass().getSimpleName() + ")");
        List<DayPrice> dayPrices = updater.get(symbol);
        Stock stock = stockDAO.findOrCreateStock(symbol);
        stock.updateDayPrices(dayPrices);
        logger.info("historical data finished: " + symbol + " (" + updater.getClass().getSimpleName() + ")");
        for (DayPrice dp : dayPrices)
            if (!dp.isValid()) logger.warning("invalid: " + dp);
    }

    private void updateInstantData(StocksEnum stocksEnum) {
        String symbol = stocksEnum.getSymbol();

        logger.info("going for instant data: " + symbol);
        InstantPrice price = yahooInstant.get(symbol);
        if (price != null) {
            Stock stock = stockDAO.findOrCreateStock(symbol);
            if (price.isValid()) {
                stock.updateInstantPrice(price);
                logger.info("instant data finished: " + symbol);
            } else {
                logger.warning("invalid: " + price);
            }
        }
    }


}
