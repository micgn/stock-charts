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

package de.mg.stock.server.dao;

import de.mg.stock.server.model.Stock;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

import static de.mg.stock.server.util.DateConverters.toDate;

@Singleton
public class StockDAO {

    private static Logger logger = Logger.getLogger(StockDAO.class.getName());

    @PersistenceContext
    private EntityManager em;

    public Stock findStock(String symbol) {
        try {
            return (Stock) em.createNamedQuery("findStockBySymbol").
                    setParameter("symbol", symbol).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Stock findOrCreateStock(String symbol) {
        Stock stock = findStock(symbol);
        if (stock == null) {
            stock = new Stock(symbol);
            stock = em.merge(stock);
        }
        return stock;
    }

    public List<Stock> findAllStocks() {
        return em.createNamedQuery("findAllStocks").getResultList();
    }

    public void deleteOldInstantData() {

        LocalDateTime keepSinceDate = LocalDateTime.now().minus(3, ChronoUnit.DAYS);
        int deleted = em.createNamedQuery("deleteOldInstantPrices").
                setParameter("keepSinceDate", toDate(keepSinceDate), TemporalType.TIMESTAMP).
                executeUpdate();

        logger.info("deleted " + deleted + " old instant prices");
    }
}
