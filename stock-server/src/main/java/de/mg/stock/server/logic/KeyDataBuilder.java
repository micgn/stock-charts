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
package de.mg.stock.server.logic;

import de.mg.stock.dto.StockKeyDataDto;
import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.dao.StockDAO;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.Stock;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.time.LocalDate.now;
import static java.util.Comparator.comparing;

@Singleton
public class KeyDataBuilder {

    private Integer[] DAY_INTERVALS = {3, 7, 30, 90, 180, 365, 2 * 365, 5 * 365, 10 * 365, 20 * 365, 30 * 365};

    @Inject
    private StockDAO stockDAO;

    public List<StockKeyDataDto> create() {

        List<StockKeyDataDto> result = new ArrayList<>();

        for (Stock stock : stockDAO.findAllStocks()) {
            List<DayPrice> all = stock.getDayPrices();

            for (int distance : DAY_INTERVALS) {

                StockKeyDataDto data = new StockKeyDataDto();
                data.setStock(StocksEnum.of(stock.getSymbol()));
                data.setDistanceInDays(distance);
                result.add(data);

                Optional<DayPrice> min = dayPricesInInterval(all, distance).min(comparing(DayPrice::getMin));
                Optional<DayPrice> max = dayPricesInInterval(all, distance).max(comparing(DayPrice::getMax));

                data.setMin(min.map(DayPrice::getMin).orElse(null));
                data.setMinDate(min.map(DayPrice::getDay).orElse(null));

                data.setMax(max.map(DayPrice::getMax).orElse(null));
                data.setMaxDate(max.map(DayPrice::getDay).orElse(null));

                if (data.getMin() != null && data.getMax() != null && data.getMin() != 0) {
                    double minToMaxPercentage = (data.getMax().doubleValue() / data.getMin().doubleValue()) - 1.0;
                    data.setMinToMaxPercentage(round(minToMaxPercentage * 100));
                }

                Optional<DayPrice> first = dayPricesInInterval(all, distance).sorted(comparing(DayPrice::getDay)).findFirst();
                Optional<DayPrice> last = dayPricesInInterval(all, distance).sorted(comparing(DayPrice::getDay).reversed()).findFirst();
                if (first.isPresent() && last.isPresent()) {
                    double percentage = last.get().getAverage().doubleValue() / first.get().getAverage().doubleValue() - 1.0;
                    data.setExactPerformancePercentage(round(percentage * 100));
                }

                if (distance >= 30) {
                    int lookAround = distance / 4;
                    if (lookAround > 60) lookAround = 60;

                    DescriptiveStatistics startStats = new DescriptiveStatistics();
                    dayPricesInInterval(all, distance).sorted(comparing(DayPrice::getDay)).
                            limit(lookAround).forEach(p -> {
                        if (p.getMin() != null) startStats.addValue(p.getMin());
                        if (p.getMax() != null) startStats.addValue(p.getMax());
                    });

                    DescriptiveStatistics endStats = new DescriptiveStatistics();
                    dayPricesInInterval(all, distance).sorted(comparing(DayPrice::getDay).reversed()).
                            limit(lookAround).forEach(p -> {
                        if (p.getMin() != null) endStats.addValue(p.getMin());
                        if (p.getMax() != null) endStats.addValue(p.getMax());
                    });

                    double startPercentile = startStats.getPercentile(0.5);
                    double endPercentile = endStats.getPercentile(0.5);

                    double percentage = (endPercentile / startPercentile) - 1.0;
                    data.setAveragePerformancePercentage(round(percentage * 100));
                }
            }
        }
        return result;
    }

    private Stream<DayPrice> dayPricesInInterval(List<DayPrice> all, int distanceInDays) {
        return all.stream().filter(dp -> dp.getDay().isAfter(now().minusDays(distanceInDays)));
    }

}
