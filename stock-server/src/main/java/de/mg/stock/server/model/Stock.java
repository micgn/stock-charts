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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@NamedQueries({
        @NamedQuery(name = "findStockBySymbol",
                query = "select s from Stock s where s.symbol = :symbol"),
        @NamedQuery(name = "findAllStocks",
                query = "select s from Stock s"),
        @NamedQuery(name = "minDate",
                query = "select min(dp.day) from DayPrice dp"),
        @NamedQuery(name = "deleteOldInstantPrices",
                query = "delete from InstantPrice ip where ip.time < :keepSinceDate")
})
@Entity
@XmlRootElement
public class Stock extends AbstractEntity {

    private static Logger logger = Logger.getLogger(Stock.class.getName());

    @Column(unique = true, nullable = false)
    private String symbol;

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

    public String getName() {
        return name;
    }

    public List<DayPrice> getDayPrices() {
        return dayPrices;
    }

    public List<InstantPrice> getInstantPrices() {
        return instantPrices;
    }

    /**
     * @return list of all day prices filled up with instant prices, in case of missing day prices
     */
    public Set<SimpleDayPrice> getAllPricesDaily() {

        getInstantPrices().stream().forEach(ip -> logger.info(ip.toString()));

        List<SimpleDayPrice> prices = Stream.concat(
                getDayPrices().stream().map(dp -> new SimpleDayPrice(dp.getDay(), dp.getAverage())),
                getInstantPrices().stream().map(ip -> new SimpleDayPrice(ip.getTime().toLocalDate(), ip.getAverage()))).
                collect(Collectors.toList());

        Set<SimpleDayPrice> result = prices.stream().
                collect(Collectors.groupingBy(SimpleDayPrice::getDate, Collectors.toSet())).
                entrySet().stream().
                map(e -> new SimpleDayPrice(e.getKey(),
                        (long) e.getValue().stream().mapToLong(SimpleDayPrice::getAverage).average().getAsDouble())).
                collect(Collectors.toSet());

        return result;
    }

    public void updateDayPrices(List<DayPrice> fetchedDayPrices, boolean overwrite) {
        if (fetchedDayPrices == null) return;
        for (DayPrice fetchedDayPrice : fetchedDayPrices) {
            if (!isContained(fetchedDayPrice, dayPrices)) {
                fetchedDayPrice.setStock(this);
                dayPrices.add(fetchedDayPrice);
            } else {
                checkForChange(fetchedDayPrice, dayPrices);

                if (overwrite) {
                    DayPrice dayPriceToUpdate = dayPrices.stream().
                            filter(dayPrice -> dayPrice.getDay().equals(fetchedDayPrice.getDay())).
                            collect(Collectors.toList()).get(0);
                    dayPriceToUpdate.setMax(fetchedDayPrice.getMax());
                    dayPriceToUpdate.setMin(fetchedDayPrice.getMin());
                }
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
                    if (!samePrices(fetchedDayPrice, dayPrice))
                        logger.warning("prices changed!\nfetched: " + fetchedDayPrice + ", persisted: " + dayPrice);
            });
        }
    }

    private static boolean samePrices(DayPrice p1, DayPrice p2) {
        if (p1 == null) return p2 == null;
        return samePrices(p1.getMax(), p2.getMax()) && samePrices(p1.getMin(), p2.getMin());
    }

    private static boolean samePrices(Long l1, Long l2) {
        if (l1 == null) return l2 == null;
        return l1.equals(l2);
    }

    public void updateInstantPrice(InstantPrice price) {

        if (instantPrices.isEmpty()) {
            instantPrices.add(price);
            price.setStock(this);

        } else if (instantPrices.stream().noneMatch(p -> p.getTime().equals(price.getTime()))) {

            // TODO remove sorting side effect
            instantPrices.sort(comparing(InstantPrice::getTime));
            InstantPrice last = instantPrices.get(instantPrices.size() - 1);

            // only add instant price if the price changed and the time is after the already fetched time
            // (the later is due to a bug when fetching data from yahoo)
            if (!last.hasSamePrices(price) && last.getTime().isBefore(price.getTime())) {
                instantPrices.add(price);
                price.setStock(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return new EqualsBuilder()
                .append(symbol, stock.symbol)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(symbol)
                .toHashCode();
    }

    public String toString() {
        return String.format("%s: %d day prices and %d instant prices", symbol, dayPrices.size(), instantPrices.size());
    }

}

