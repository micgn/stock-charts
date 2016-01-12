package de.mg.stock.server.model;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class DayPrice {

    private Stock stock;
    private LocalDate day;
    private Long start;
    private Long end;
    private Long max;
    private Long min;

    public DayPrice(Stock stock, LocalDate day) {
        this.stock = stock;
        this.day = day;
    }

    public LocalDate getDay() {
        return day;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }
}
