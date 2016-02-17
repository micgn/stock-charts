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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
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
        return new EqualsBuilder()
                .append(stock, dayPrice.stock)
                .append(day, dayPrice.day)
                .append(max, dayPrice.max)
                .append(min, dayPrice.min)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(stock)
                .append(day)
                .append(max)
                .append(min)
                .toHashCode();
    }
}
