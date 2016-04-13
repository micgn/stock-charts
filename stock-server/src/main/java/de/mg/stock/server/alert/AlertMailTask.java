package de.mg.stock.server.alert;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.AlertDAO;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.SimpleDayPrice;
import de.mg.stock.server.model.Stock;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.mg.stock.server.util.DateConverters.isSameDay;
import static de.mg.stock.server.util.DateConverters.toDate;
import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.util.Comparator.comparing;
import static javax.ejb.TransactionAttributeType.REQUIRED;

@Stateless
@TransactionAttribute(REQUIRED)
public class AlertMailTask {

    public static final int DAYS_BACK = 5;
    public static final long PERCENTAGE_THRESHOLD = 10;

    @Inject
    private StockDAO stockDAO;

    @Inject
    private AlertDAO alertDAO;

    @Inject
    private AlertMailSender alertMailSender;


    @Schedule(minute = "10, 30, 50", persistent = false)
    public void checkForAlert() {

        final Date today = toDate(LocalDate.now());

        Optional<Date> lastAlert = alertDAO.getLastAlertSent();
        if (lastAlert.isPresent() && isSameDay(lastAlert.get(), today))
            return;

        Map<StocksEnum, Long> alerts = calculateChanges(DAYS_BACK, PERCENTAGE_THRESHOLD);
        if (!alerts.isEmpty()) {
            alertMailSender.send(alerts, "Stock Alert");
            alertDAO.setLastAlertSent(today);
        }
    }

    @Schedule(dayOfWeek = "5", persistent = false)
    public void sendWeeklyMail() {
        Map<StocksEnum, Long> changes = calculateChanges(7, 0);
        alertMailSender.send(changes, "Weekly Stock Changes");
    }

    private Map<StocksEnum, Long> calculateChanges(int daysBack, long thresholdPercentage) {

        final LocalDateTime thisMorning = LocalDate.now().atStartOfDay();
        final LocalDate startDay = LocalDate.now().minus(daysBack, ChronoUnit.DAYS);
        final Predicate<SimpleDayPrice> withinDaysBack = d ->
                d.getDate().isEqual(startDay) ||
                        (d.getDate().isAfter(startDay) && d.getDate().isBefore(thisMorning.toLocalDate()));

        Map<StocksEnum, Long> result = new HashMap<>();
        for (Stock stock : stockDAO.findAllStocks()) {

            Optional<InstantPrice> lastInstantPrice = stock.getInstantPrices().stream().
                    max(comparing(InstantPrice::getTime));

            Long lastAvg;
            if (lastInstantPrice.isPresent() &&
                    lastInstantPrice.get().getTime().isAfter(thisMorning) &&
                    (lastAvg = lastInstantPrice.get().getAverage()) != null) {

                Long maxPercentage = minMaxStream(stock, lastAvg, thresholdPercentage, withinDaysBack).
                        max(Long::compareTo).orElse(0L);
                Long minPercentage = minMaxStream(stock, lastAvg, thresholdPercentage, withinDaysBack).
                        min(Long::compareTo).orElse(0L);
                Long change = abs(maxPercentage) > abs(minPercentage) ? maxPercentage : minPercentage;

                if (change != 0) result.put(StocksEnum.of(stock.getSymbol()), change);
            }
        }
        return result;
    }

    private Stream<Long> minMaxStream(Stock stock, Long lastAvg, long thresholdPercentage,
                                      Predicate<SimpleDayPrice> withinDaysBack) {

        return stock.getAllPricesDaily().stream().
                filter(withinDaysBack).
                map(SimpleDayPrice::getAverage).
                map(avg -> 1d * (lastAvg - avg) / lastAvg).
                map(avgPercent -> round(100 * avgPercent)).
                filter(avg -> abs(avg) >= thresholdPercentage);
    }

}
