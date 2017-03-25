package de.mg.stock.server.alert;

import de.mg.stock.dto.StockKeyDataDto;
import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.AlertDAO;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.logic.AlertCalculator;
import de.mg.stock.server.logic.KeyDataBuilder;
import de.mg.stock.server.util.DateTimeProvider;
import de.mg.stock.server.util.KeyDataStringBuilder;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
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

    @Inject
    private KeyDataBuilder keyDataBuilder;

    @Inject
    private KeyDataStringBuilder keyDataStringBuilder;


    @Schedule(minute = "10, 30, 50", persistent = false)
    public void checkForAlert() {

        final Date today = toDate(dateTimeProvider.today());

        Optional<Date> lastAlert = alertDAO.getLastAlertSent();
        if (lastAlert.isPresent() && isSameDay(lastAlert.get(), today))
            return;

        Map<StocksEnum, Long> changePercent = alertCalculator.immediateToNofifyChanges();
        if (!changePercent.isEmpty()) {
            alertMailSender.send(formatMsg(changePercent), "Stock Alert");
            alertDAO.setLastAlertSent(today);
        }
    }

    @Schedule(dayOfWeek = "5", persistent = false)
    public void sendWeeklyMail() {
        List<StockKeyDataDto> stockKeyData = keyDataBuilder.create();
        String str = keyDataStringBuilder.asHtml(stockKeyData);
        alertMailSender.send(str, "Weekly Stock Changes");
    }

    private String formatMsg(Map<StocksEnum, Long> changes) {
        StringBuilder msg = new StringBuilder();
        for (StocksEnum stock : changes.keySet()) {
            msg.append(stock.getName()).append(" --> ").append(changes.get(stock)).append("%\n");
        }
        return msg.toString();
    }

}
