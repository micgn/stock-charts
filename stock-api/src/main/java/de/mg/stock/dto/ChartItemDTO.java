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

    public void setInstantPrice(boolean instantPrice) {
        this.instantPrice = instantPrice;
    }

    public boolean isValid() {
        boolean valid = true;
        if (minLong != null && maxLong != null)
            valid = minLong <= maxLong;
        if (minLong != null && averageLong != null)
            valid = valid && minLong <= averageLong;
        if (averageLong != null && maxLong != null)
            valid = valid && averageLong <= maxLong;
        return valid;
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

    public static class Builder {
        private LocalDateTime dateTime;
        private Long minLong;
        private Long maxLong;
        private Long averageLong;
        private boolean instantPrice;

        public Builder setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder setMinLong(Long minLong) {
            this.minLong = minLong;
            return this;
        }

        public Builder setMaxLong(Long maxLong) {
            this.maxLong = maxLong;
            return this;
        }

        public Builder setAverageLong(Long averageLong) {
            this.averageLong = averageLong;
            return this;
        }

        public Builder setInstantPrice(boolean instantPrice) {
            this.instantPrice = instantPrice;
            return this;
        }

        public ChartItemDTO build() {
            return new ChartItemDTO(dateTime, minLong, maxLong, averageLong, instantPrice);
        }
    }
}
