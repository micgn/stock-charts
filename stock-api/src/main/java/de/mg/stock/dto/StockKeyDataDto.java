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

import java.time.LocalDateTime;


public class StockKeyDataDto {

    private StocksEnum stock;
    private Integer distanceInDays;
    private Long min, max;
    private Long minToMaxPercentage;
    private LocalDateTime minDate, maxDate;
    private Long exactPerformancePercentage, averagePerformancePercentage;

    public StocksEnum getStock() {
        return stock;
    }

    public void setStock(StocksEnum stock) {
        this.stock = stock;
    }

    public Integer getDistanceInDays() {
        return distanceInDays;
    }

    public void setDistanceInDays(Integer distanceInDays) {
        this.distanceInDays = distanceInDays;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getMinToMaxPercentage() {
        return minToMaxPercentage;
    }

    public void setMinToMaxPercentage(Long minToMaxPercentage) {
        this.minToMaxPercentage = minToMaxPercentage;
    }

    public LocalDateTime getMinDate() {
        return minDate;
    }

    public void setMinDate(LocalDateTime minDate) {
        this.minDate = minDate;
    }

    public LocalDateTime getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(LocalDateTime maxDate) {
        this.maxDate = maxDate;
    }

    public Long getExactPerformancePercentage() {
        return exactPerformancePercentage;
    }

    public void setExactPerformancePercentage(Long exactPerformancePercentage) {
        this.exactPerformancePercentage = exactPerformancePercentage;
    }

    public Long getAveragePerformancePercentage() {
        return averagePerformancePercentage;
    }

    public void setAveragePerformancePercentage(Long averagePerformancePercentage) {
        this.averagePerformancePercentage = averagePerformancePercentage;
    }

    @Override
    public String toString() {
        return "StockKeyDataDto{" +
                "stock=" + stock +
                ", distanceInDays=" + distanceInDays +
                ", min=" + min +
                ", max=" + max +
                ", minToMaxPercentage=" + minToMaxPercentage +
                ", minDate=" + minDate +
                ", maxDate=" + maxDate +
                ", exactPerformancePercentage=" + exactPerformancePercentage +
                ", averagePerformancePercentage=" + averagePerformancePercentage +
                '}';
    }
}
