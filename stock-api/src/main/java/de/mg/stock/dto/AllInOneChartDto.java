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

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class AllInOneChartDto {

    private List<AllInOneChartItemDto> items = new ArrayList<>();

    public AllInOneChartDto() {
    }

    public List<AllInOneChartItemDto> getItems() {
        return items;
    }

    public void setItems(List<AllInOneChartItemDto> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "AllInOneChartDto{" +
                "items=" + items +
                '}';
    }

    public LocalDateTime lastDate() {
        Optional<AllInOneChartItemDto> minDateDto = items.stream().
                min((i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime()));
        if (minDateDto.isPresent()) return minDateDto.get().getDateTime();
        else return null;
    }
}
