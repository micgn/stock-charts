package de.mg.stock.server;

import de.mg.stock.server.model.Stock;
import de.mg.stock.server.util.DateConverters;
import org.apache.commons.lang3.time.DateUtils;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Date;
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

    public LocalDate minDate() {
        Date date = (Date) em.createNamedQuery("minDate").getSingleResult();
        return DateConverters.toLocalDate(date);
    }

    public List<Stock> findAllStocks() {
        return em.createNamedQuery("findAllStocks").getResultList();
    }
}
