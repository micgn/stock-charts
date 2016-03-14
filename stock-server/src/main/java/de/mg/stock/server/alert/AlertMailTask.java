package de.mg.stock.server.alert;

import de.mg.stock.server.dao.StockDAO;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

import static javax.ejb.TransactionAttributeType.REQUIRED;

@Stateless
@TransactionAttribute(REQUIRED)
public class AlertMailTask {

    @Inject
    private StockDAO stockDAO;

    @Inject
    private AlertMail alertMail;


    @Schedule(minute = "10, 30, 50", persistent = false)
    public void checkForAlert() {

        // TODO
        alertMail.send(null);
    }
}
