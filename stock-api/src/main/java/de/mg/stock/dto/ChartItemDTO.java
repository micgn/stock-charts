package de.mg.stock.dto;

import de.mg.stock.util.LocalDateTimeAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ChartItemDTO implements Serializable {

    private LocalDateTime dateTime;
    private Long minLong;
    private Long maxLong;
    private Long averageLong;
    private boolean instantPrice;

    public ChartItemDTO() {
    }

    public ChartItemDTO(LocalDateTime dateTime, Long minLong, Long maxLong, Long averageLong, boolean instantPrice) {
        this.dateTime = dateTime;
        this.minLong = minLong;
        this.maxLong = maxLong;
        this.averageLong = averageLong;
        this.instantPrice = instantPrice;
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isInstantPrice() {
        return instantPrice;
    }

    public void setInstantPrice(boolean instantPrice) {
        this.instantPrice = instantPrice;
    }

    public Long getMinLong() {
        return minLong;
    }

    public Double getMin() {
        return (minLong != null) ? minLong / 100.0 : null;
    }

    public Long getMaxLong() {
        return maxLong;
    }

    public Double getMax() {
        return (maxLong != null) ? maxLong / 100.0 : null;
    }

    public Long getAverageLong() {
        return averageLong;
    }

    public Double getAverage() {
        return (averageLong != null) ? averageLong / 100.0 : null;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setMinLong(Long minLong) {
        this.minLong = minLong;
    }

    public void setMaxLong(Long maxLong) {
        this.maxLong = maxLong;
    }

    public void setAverageLong(Long averageLong) {
        this.averageLong = averageLong;
    }

    @Override
    public String toString() {
        return "ChartItemDTO{" +
                "dateTime=" + dateTime +
                ", minLong=" + minLong +
                ", maxLong=" + maxLong +
                ", averageLong=" + averageLong +
                ", instantPrice=" + instantPrice +
                '}';
    }
}
