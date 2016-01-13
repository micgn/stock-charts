package de.mg.stock.server.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "findStockBySymbol",
                query = "select s from Stock s where s.symbol = :symbol"),
        @NamedQuery(name = "findAllStocks",
                query = "select s from Stock s"),
        @NamedQuery(name = "minDate",
                query = "select min(dp.day) from DayPrice dp")
})
@Entity
@XmlRootElement
public class Stock extends AbstractEntity {

    private static Logger logger = Logger.getLogger(Stock.class.getName());

    @Column(unique = true, nullable = false)
    private String symbol;

    // TODO remove
    private String name;

    @OneToMany(mappedBy = "stock", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final List<DayPrice> dayPrices = new ArrayList<>();

    @OneToMany(mappedBy = "stock", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final List<InstantPrice> instantPrices = new ArrayList<>();

    public Stock() {
    }

    public Stock(String symbol) {
        this.symbol = symbol;
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

    public void updateDayPrices(List<DayPrice> fetchedDayPrices) {
        if (fetchedDayPrices == null) return;
        for (DayPrice fetchedDayPrice : fetchedDayPrices) {
            if (!isContained(fetchedDayPrice, dayPrices)) {
                fetchedDayPrice.setStock(this);
                dayPrices.add(fetchedDayPrice);
            } else {
                checkForChange(fetchedDayPrice, dayPrices);
            }
        }
    }

    private boolean isContained(DayPrice fetchedDayPrice, List<DayPrice> dayPrices) {
        return dayPrices.stream().anyMatch(dayPrice -> dayPrice.getDay().equals(fetchedDayPrice.getDay()));
    }

    private void checkForChange(DayPrice fetchedDayPrice, List<DayPrice> dayPrices) {
        if (logger.isLoggable(Level.WARNING)) {
            dayPrices.stream().forEach(dayPrice ->
            {
                if (dayPrice.getDay().equals(fetchedDayPrice.getDay()))
                    if (!fetchedDayPrice.equals(dayPrice))
                        logger.warning("prices changed!\nfetched: " + fetchedDayPrice + ", persisted: " + dayPrice);
            });
        }
    }


    public void updateInstantPrice(InstantPrice price) {

        // cleanup
        Set<InstantPrice> toRemove = instantPrices.stream()
                .filter(p -> p.getTime().isBefore(LocalDate.now().minus(5, ChronoUnit.DAYS).atStartOfDay()))
                .collect(Collectors.toSet());
        instantPrices.removeAll(toRemove);
        toRemove.stream().forEach(p -> p.setStock(null));

        if (instantPrices.isEmpty()) {
            instantPrices.add(price);
            price.setStock(this);

        } else if (!instantPrices.stream().anyMatch(p -> p.getTime().equals(price.getTime()))) {

            instantPrices.sort((o1, o2) -> o1.getTime().compareTo(o2.getTime()));
            InstantPrice prev = instantPrices.get(instantPrices.size() - 1);
            if (!prev.hasSamePrices(price)) {
                instantPrices.add(price);
                price.setStock(this);
            }
        }
    }
}

