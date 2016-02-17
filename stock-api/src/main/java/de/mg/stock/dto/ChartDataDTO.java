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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ChartDataDTO implements Serializable {

    private String name;
    private LocalDateTime lastUpdate;
    private List<ChartItemDTO> items = new ArrayList<>();

    public ChartDataDTO() {
    }

    public ChartDataDTO(String name, LocalDateTime lastUpdate) {
        this.name = name;
        this.lastUpdate = lastUpdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setItems(List<ChartItemDTO> items) {
        this.items = items;
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public List<ChartItemDTO> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ChartDataDTO{" +
                "name='" + name + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", items=" + items +
                '}';
    }
}
