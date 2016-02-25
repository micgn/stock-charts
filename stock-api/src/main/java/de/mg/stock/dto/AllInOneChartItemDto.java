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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AllInOneChartItemDto {

    private LocalDateTime dateTime;
    private Map<StocksEnum, Long> averageLongs = new HashMap<>();

    public AllInOneChartItemDto() {
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Map<StocksEnum, Long> getAverageLongs() {
        return averageLongs;
    }

    public void setAverageLongs(Map<StocksEnum, Long> averageLongs) {
        this.averageLongs = averageLongs;
    }

    public void addAverageLong(StocksEnum stock, long average) {
        this.averageLongs.put(stock, average);
    }

    public Double getAverage(StocksEnum stock) {
        return (averageLongs.get(stock) != null) ? averageLongs.get(stock) / 100.0 : null;
    }

}
