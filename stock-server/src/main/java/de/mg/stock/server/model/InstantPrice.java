package de.mg.stock.server.model;

import javax.persistence.Entity;
import java.time.LocalTime;

@Entity
public class InstantPrice {

    private Stock stock;
    private LocalTime time;
    private Long ask;
    private Long bid;
    private Long dayMin;
    private Long dayMax;

    public InstantPrice(Stock stock, LocalTime time) {
        this.stock = stock;
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
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
}
