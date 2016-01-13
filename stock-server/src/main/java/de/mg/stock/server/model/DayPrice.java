package de.mg.stock.server.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

import static de.mg.stock.server.util.DateConverters.toDate;
import static de.mg.stock.server.util.DateConverters.toLocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "day"}))
public class DayPrice extends AbstractPrice {

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date day;

    @Column(name = "maximum", nullable = false)
    private Long max;

    @Column(name = "minimum", nullable = false)
    private Long min;

    public DayPrice() {
    }

    public DayPrice(LocalDate day) {
        this.day = toDate(day);
    }

    public DayPrice(LocalDate day, Long min, Long max) {
        this.day = toDate(day);
        this.max = max;
        this.min = min;
    }

    public LocalDate getDay() {
        return toLocalDate(day);
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

    public Long getAverage() {
        if (min == null) return max;
        if (max == null) return min;
        return (min.longValue() + max.longValue()) / 2;
    }

    @Override
    public String toString() {
        return "DayPrice{" +
                "day=" + day +
                ", max=" + max +
                ", min=" + min +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayPrice dayPrice = (DayPrice) o;

        if (!day.equals(dayPrice.day)) return false;
        if (max != null ? !max.equals(dayPrice.max) : dayPrice.max != null) return false;
        return min != null ? min.equals(dayPrice.min) : dayPrice.min == null;

    }

    @Override
    public int hashCode() {
        int result = day.hashCode();
        result = 31 * result + (max != null ? max.hashCode() : 0);
        result = 31 * result + (min != null ? min.hashCode() : 0);
        return result;
    }
}
