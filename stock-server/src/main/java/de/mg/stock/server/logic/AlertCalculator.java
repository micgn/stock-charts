package de.mg.stock.server.logic;


import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.SimpleDayPrice;
import de.mg.stock.server.model.Stock;
import de.mg.stock.server.util.DateTimeProvider;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Singleton
public class AlertCalculator {

    private static Logger LOG = Logger.getLogger(AlertCalculator.class.getName());

    @Inject
    private StockDAO stockDAO;

    @Inject
    private DateTimeProvider dateTimeProvider;


    public Map<StocksEnum, Long> immediateToNofifyChanges() {
        return changes(5, true, 10);
    }

    public Map<StocksEnum, Long> weeklyChanges() {
        return changes(7, false, 0);
    }

    private Map<StocksEnum, Long> changes(int daysBack, boolean onlyTodaysPrices, long percentageThreshold) {

        final LocalDateTime thisMorning = dateTimeProvider.today().atStartOfDay();
        final LocalDate startDate = dateTimeProvider.today().minus(daysBack, ChronoUnit.DAYS);
        final Predicate<SimpleDayPrice> withinDaysBack = dayPrice -> {
            LocalDate date = dayPrice.getDate();
            return date.isEqual(startDate) ||
                    (date.isAfter(startDate) && date.isBefore(thisMorning.toLocalDate()));
        };


        Map<StocksEnum, Long> result = new HashMap<>();
        for (Stock stock : stockDAO.findAllStocks()) {

            Optional<InstantPrice> lastPrice = getLastPrice(stock, onlyTodaysPrices, thisMorning);
            if (!lastPrice.isPresent()) {
                LOG.warning("no last price found");
                continue;
            }

            Long lastAvg = lastPrice.get().getAverage();
            List<Long> relevantPricePercentages = relevantPricePercentages(stock, lastAvg, percentageThreshold, withinDaysBack);
            if (relevantPricePercentages.size() == 0) {
                LOG.info("no relevant prices found for " + stock.getSymbol() + " and threshold " + percentageThreshold);
            } else {
                LOG.info(relevantPricePercentages.size() + " relevant prices found for threshold " + percentageThreshold);
            }
            Long relevantMax = relevantPricePercentages.stream().max(Long::compareTo).orElse(0L);
            Long relevantMin = relevantPricePercentages.stream().min(Long::compareTo).orElse(0L);

            Long change = abs(relevantMax) > abs(relevantMin) ? relevantMax : relevantMin;
            LOG.info("found percentage change for " + stock.getSymbol() + ": " + change);

            if (change != 0L)
                result.put(StocksEnum.of(stock.getSymbol()), change);

        }
        return result;
    }

    private static Optional<InstantPrice> getLastPrice(Stock stock, boolean onlyTodaysPrices, LocalDateTime thisMorning) {

        Optional<InstantPrice> lastInstantPrice = stock.getInstantPrices().stream().
                max(comparing(InstantPrice::getTime));

        if (!lastInstantPrice.isPresent() || lastInstantPrice.get().getAverage() == null) {

            LOG.warning("no last instant price for " + stock.getSymbol());

            if (onlyTodaysPrices)
                return Optional.empty();

            Optional<DayPrice> lastDayPriceOpt = stock.getDayPrices().stream().max(comparing(DayPrice::getDay));
            if (!lastDayPriceOpt.isPresent()) {
                LOG.warning("no last day price for " + stock.getSymbol());
                return Optional.empty();
            }

            DayPrice lastDayPrice = lastDayPriceOpt.get();
            return Optional.of(new InstantPrice(lastDayPrice.getDay().atStartOfDay(), lastDayPrice.getMin(),
                    lastDayPrice.getMax()));
        }

        if (!onlyTodaysPrices || lastInstantPrice.get().getTime().isAfter(thisMorning)) {
            return lastInstantPrice;
        } else {
            LOG.info("last price not after this morning for " + stock.getSymbol());
            return Optional.empty();
        }
    }

    private static List<Long> relevantPricePercentages(Stock stock, Long lastPrice, long percentageThreshold,
                                                       Predicate<SimpleDayPrice> predicate) {

        return stock.getAllPricesDaily().stream().
                filter(predicate).
                map(SimpleDayPrice::getAverage).
                map(avg -> 1d * (lastPrice - avg) / lastPrice).
                map(avg -> round(100 * avg)).
                filter(avg -> abs(avg) >= percentageThreshold).collect(toList());
    }

}
