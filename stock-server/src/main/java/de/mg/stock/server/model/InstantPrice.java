package de.mg.stock.server.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

import static de.mg.stock.server.util.DateConverters.toDate;
import static de.mg.stock.server.util.DateConverters.toLocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "time"}))
public class InstantPrice extends AbstractPrice {

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    private Long ask;
    private Long bid;
    private Long dayMin;
    private Long dayMax;

    public InstantPrice() {
    }

    public LocalDateTime getTime() {
        return toLocalDateTime(time);
    }

    public void setTime(LocalDateTime time) {
        this.time = toDate(time);
    }

    public Long getAsk() {
        return ask;
    }

    public void setAsk(Long ask) {
        this.ask = ask;
    }

    public Long getBid() {
        return bid;
    }

    public void setBid(Long bid) {
        this.bid = bid;
    }

    public Long getDayMin() {
        return dayMin;
    }

    public void setDayMin(Long dayMin) {
        this.dayMin = dayMin;
    }

    public Long getDayMax() {
        return dayMax;
    }

    public void setDayMax(Long dayMax) {
        this.dayMax = dayMax;
    }


    public boolean hasSamePrices(InstantPrice that) {

        if (ask != null ? !ask.equals(that.ask) : that.ask != null) return false;
        if (bid != null ? !bid.equals(that.bid) : that.bid != null) return false;
        if (dayMin != null ? !dayMin.equals(that.dayMin) : that.dayMin != null) return false;
        return dayMax != null ? dayMax.equals(that.dayMax) : that.dayMax == null;
    }
}
