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

import static de.mg.stock.server.util.DateConverters.isSameDay;
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

        final Date today = new Date();

        Optional<Date> lastAlert = alertDAO.getLastAlertSent();
        if (lastAlert.isPresent() && isSameDay(lastAlert.get(), today))
            return;

        Map<StocksEnum, Long> alerts = calculateChanges();
        if (!alerts.isEmpty()) {
            alertMailSender.send(alerts);
            alertDAO.setLastAlertSent(today);
        }
    }

    private Map<StocksEnum, Long> calculateChanges() {

        final LocalDateTime thisMorning = LocalDate.now().atStartOfDay();
        final LocalDate startDay = LocalDate.now().minus(DAYS_BACK, ChronoUnit.DAYS);
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

                Optional<Long> maxPercentage = stock.getAllPricesDaily().stream().
                        filter(withinDaysBack).
                        map(SimpleDayPrice::getAverage).
                        map(avg -> 1d * (avg - lastAvg) / lastAvg).
                        map(avg -> Math.round(100 * avg)).
                        filter(avg -> Math.abs(avg) >= PERCENTAGE_THRESHOLD).
                        max(Long::compareTo);

                if (maxPercentage.isPresent())
                    result.put(StocksEnum.of(stock.getSymbol()), maxPercentage.get());
            }
        }
        return result;
    }

}
