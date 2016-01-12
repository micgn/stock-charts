package de.mg.stock.server.model;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Stock {

    private String symbol;
    private String name;
    private final List<DayPrice> dayPrices = new ArrayList<>();
    private final List<InstantPrice> instantPrices  = new ArrayList<>();

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DayPrice> getDayPrices() {
        return dayPrices;
    }

    public List<InstantPrice> getInstantPrices() {
        return instantPrices;
    }

}
