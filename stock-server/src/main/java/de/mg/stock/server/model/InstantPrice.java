/*
 * Copyright 2016 Michael Gnatz.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.mg.stock.server.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    /**
     * buy price
     */
    private Long ask;

    /**
     * sell price
     */
    private Long bid;

    private Long dayMin;
    private Long dayMax;

    public InstantPrice() {
    }

    public InstantPrice(LocalDateTime time, long min, long max) {
        this.time = toDate(time);
        this.ask = min;
        this.bid = max;
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

    public Long getMin() {
        if (ask == null) return bid;
        if (bid == null) return ask;
        return (ask.longValue() < bid.longValue()) ? ask : bid;
    }

    public Long getMax() {
        if (ask == null) return bid;
        if (bid == null) return ask;
        return (ask.longValue() > bid.longValue()) ? ask : bid;
    }

    public Long getAverage() {
        Long result;
        if (getMax() == null)
            result = getMin();
        else if (getMin() == null)
            result = getMax();
        else
            result = (getMin() + getMax()) / 2;

        if (result == null)
            if (dayMax == null)
                result = dayMin;
            else if (dayMin == null)
                result = dayMax;
            else
                result = (getDayMin() + getDayMax()) / 2;

        return result;
    }

    public boolean hasSamePrices(InstantPrice that) {

        if (ask != null ? !ask.equals(that.ask) : that.ask != null) return false;
        if (bid != null ? !bid.equals(that.bid) : that.bid != null) return false;
        if (dayMin != null ? !dayMin.equals(that.dayMin) : that.dayMin != null) return false;
        return dayMax != null ? dayMax.equals(that.dayMax) : that.dayMax == null;
    }

    public boolean isValid() {
        boolean valid = true;
        if (dayMax != null && dayMin != null) {
            valid = dayMin <= dayMax;
        }
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstantPrice that = (InstantPrice) o;
        return new EqualsBuilder()
                .append(stock, that.stock)
                .append(time, that.time)
                .append(ask, that.ask)
                .append(bid, that.bid)
                .append(dayMin, that.dayMin)
                .append(dayMax, that.dayMax)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(stock)
                .append(time)
                .append(ask)
                .append(bid)
                .append(dayMin)
                .append(dayMax)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "InstantPrice{" +
                "time=" + time +
                ", ask=" + ask +
                ", bid=" + bid +
                ", dayMin=" + dayMin +
                ", dayMax=" + dayMax +
                '}';
    }


}
