package de.mg.stock.server.alert;

import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.AlertDAO;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.logic.AlertCalculator;
import de.mg.stock.server.util.DateTimeProvider;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static de.mg.stock.server.util.DateConverters.isSameDay;
import static de.mg.stock.server.util.DateConverters.toDate;
import static javax.ejb.TransactionAttributeType.REQUIRED;

@Stateless
@TransactionAttribute(REQUIRED)
public class AlertMailTask {


    @Inject
    private StockDAO stockDAO;

    @Inject
    private AlertDAO alertDAO;

    @Inject
    private AlertMailSender alertMailSender;

    @Inject
    private AlertCalculator alertCalculator;

    @Inject
    private DateTimeProvider dateTimeProvider;


    @Schedule(minute = "10, 30, 50", persistent = false)
    public void checkForAlert() {

        final Date today = toDate(dateTimeProvider.today());

        Optional<Date> lastAlert = alertDAO.getLastAlertSent();
        if (lastAlert.isPresent() && isSameDay(lastAlert.get(), today))
            return;

        Map<StocksEnum, Long> alerts = alertCalculator.immediateToNofifyChanges();
        if (!alerts.isEmpty()) {
            alertMailSender.send(alerts, "Stock Alert");
            alertDAO.setLastAlertSent(today);
        }
    }

    @Schedule(dayOfWeek = "5", persistent = false)
    public void sendWeeklyMail() {
        Map<StocksEnum, Long> changes = alertCalculator.weeklyChanges();
        alertMailSender.send(changes, "Weekly Stock Changes");
    }

}
