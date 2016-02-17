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
import java.util.List;

@Singleton
public class StockDAO {

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

    public List<Stock> findAllStocks() {
        return em.createNamedQuery("findAllStocks").getResultList();
    }
}
