package de.mg.stock.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
public class AlertMailStatus extends AbstractEntity {

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastAlertSent;

    public Date getLastAlertSent() {
        return lastAlertSent;
    }

    public void setLastAlertSent(Date lastAlertSent) {
        this.lastAlertSent = lastAlertSent;
    }
}
