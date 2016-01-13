package de.mg.stock.server.update;

import de.mg.stock.server.model.DayPrice;

import java.util.List;

public interface StockUpdaterHistorical {

    List<DayPrice> get(String symbol);
}
