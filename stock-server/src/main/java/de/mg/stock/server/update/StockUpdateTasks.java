package de.mg.stock.server.update;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.Stock;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

    @Inject
    private StockDAO stockDAO;

    @Schedule(hour = "5, 22")
    public void updateHistoricalData() {
        //updateHistoricalData(WORLD, google);
        //updateHistoricalData(SMALL200, google);
        updateHistoricalData(EMERGING, google);

        updateHistoricalData(WORLD, yahoo);
        //updateHistoricalData(EMERGING, yahoo);
        updateHistoricalData(SMALL200, yahoo);
    }

    @Schedule(hour = "7-23", minute = "5, 15, 25, 35, 45, 55")
    public void updateInstantData() {
        updateInstantData(WORLD);
        updateInstantData(SMALL200);
        updateInstantData(EMERGING);
    }


    @Asynchronous
    public void updateAsync() {
        updateInstantData();
        updateHistoricalData();
    }

    private void updateHistoricalData(StocksEnum stocksEnum, StockUpdaterHistorical updater) {
        String symbol = stocksEnum.getSymbol();

        logger.info("going for historical data: " + symbol + " (" + updater.getClass().getSimpleName() + ")");
        List<DayPrice> dayPrices = updater.get(symbol);
        Stock stock = findOrCreateStock(symbol);
        stock.updateDayPrices(dayPrices);
        logger.info("historical data finished: " + symbol + " (" + updater.getClass().getSimpleName() + ")");
    }

    private void updateInstantData(StocksEnum stocksEnum) {
        String symbol = stocksEnum.getSymbol();

        logger.info("going for instant data: " + symbol);
        InstantPrice price = yahooInstant.get(symbol);
        if (price != null) {
            Stock stock = findOrCreateStock(symbol);
            stock.updateInstantPrice(price);
            logger.info("instant data finished: " + symbol);
        }
    }

    private Stock findOrCreateStock(String symbol) {
        Stock stock = stockDAO.findStock(symbol);
        if (stock == null) {
            stock = new Stock(symbol);
            stock = em.merge(stock);
        }
        return stock;
    }
}
