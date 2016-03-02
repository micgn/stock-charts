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

import de.mg.stock.dto.AllInOneChartDto;
import de.mg.stock.dto.AllInOneChartItemDto;
import de.mg.stock.dto.ChartDataDTO;
import de.mg.stock.dto.ChartItemDTO;
import de.mg.stock.dto.StocksEnum;
import de.mg.stock.server.model.DayPrice;
import de.mg.stock.server.model.InstantPrice;
import de.mg.stock.server.model.Stock;

import javax.ejb.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Singleton
public class ChartBuilder {

    public ChartDataDTO createOne(Stock stock, int points, Optional<LocalDate> since, boolean percentages) {

        ChartDataDTO dto = new ChartDataDTO(stock.getName(), LocalDateTime.now());

        int dayPoints, instantPoints;
        if (stock.getInstantPrices().size() > 0) {
            dayPoints = (int) (points * 0.8);
            instantPoints = (int) (points * 0.2);
        } else {
            dayPoints = points;
            instantPoints = 0;
        }

        Optional<InstantPrice> firstOpt = stock.getInstantPrices().stream().max((p1, p2) -> p1.getTime().compareTo(p2.getTime()));
        final LocalDateTime firstInstantPrice = (firstOpt.isPresent()) ? firstOpt.get().getTime() : LocalDateTime.MAX;

        Predicate<DayPrice> isAfterSinceAndBeforeInstantPrices = (DayPrice dp) -> {
            boolean isAfterSince = (!since.isPresent()) ? true : dp.getDay().isAfter(since.get()) || dp.getDay().isEqual(since.get());
            return isAfterSince && dp.getDay().plus(1, ChronoUnit.DAYS).atStartOfDay().isBefore(firstInstantPrice);
        };

        List<ChartItemDTO> dayItems = new ArrayList<>();
        stock.getDayPrices().stream().
                filter(isAfterSinceAndBeforeInstantPrices).
                sorted((p1, p2) -> p1.getDay().compareTo(p2.getDay())).
                forEach(dp ->
                        dayItems.add(new ChartItemDTO(dp.getDay().atStartOfDay(), dp.getMin(), dp.getMax(), average(dp.getMin(), dp.getMax()), false))
                );
        aggregate(dayItems, dayPoints);

        List<ChartItemDTO> instantItems = new ArrayList<>();
        stock.getInstantPrices().stream().
                sorted((p1, p2) -> p1.getTime().compareTo(p2.getTime())).
                forEach(ip ->
                        instantItems.add(new ChartItemDTO(ip.getTime(), ip.getMin(), ip.getMax(), average(ip.getMin(), ip.getMax()), true))
                );
        aggregate(instantItems, instantPoints);

        dto.getItems().addAll(dayItems);
        dto.getItems().addAll(instantItems);

        if (percentages)
            transformIntoPercentages(dto.getItems());

        return dto;
    }

    public ChartDataDTO createAggregated(List<Stock> stocks, List<Integer> stockWeights, int points, Optional<LocalDate> since) {

        if (stocks.size() < 2)
            throw new RuntimeException("at least 2 stocks need to be aggregated");
        if (stocks.size() != stockWeights.size())
            throw new RuntimeException("different amounts of stocks and stock weights");

        Map<LocalDate, List<Double>> avgPercentsPerDate = avgPercentsPerDate(stocks, since);

        List<ChartItemDTO> aggregatedItemList = new ArrayList<>();
        for (LocalDate date : avgPercentsPerDate.keySet()) {
            List<Double> avgPercents = avgPercentsPerDate.get(date);
            double aggregatedAvgPercent = 0;
            for (int i = 0; i < avgPercents.size(); i++) {
                double avg = avgPercents.get(i);
                aggregatedAvgPercent += avg * stockWeights.get(i) / 100.0;
            }

            long aggregatedAvgPercentLong = Math.round(aggregatedAvgPercent * 100 * 100);
            ChartItemDTO itemDto = new ChartItemDTO(date.atStartOfDay(), null, null, aggregatedAvgPercentLong, false);
            aggregatedItemList.add(itemDto);
        }

        aggregatedItemList.sort((i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime()));
        aggregate(aggregatedItemList, points);

        ChartDataDTO dto = new ChartDataDTO("aggregated", LocalDateTime.now());
        dto.getItems().addAll(aggregatedItemList);
        return dto;
    }

    public AllInOneChartDto createAllInOne(List<Stock> stocks, int points, LocalDate since) {

        Map<LocalDate, List<Double>> avgPercentsPerDate = avgPercentsPerDate(stocks, Optional.of(since));

        List<AllInOneChartItemDto> items = new ArrayList<>();
        for (LocalDate date : avgPercentsPerDate.keySet()) {
            List<Double> avgs = avgPercentsPerDate.get(date);

            AllInOneChartItemDto item = new AllInOneChartItemDto();
            item.setDateTime(date.atStartOfDay());
            for (int i = 0; i < avgs.size(); i++) {
                double avg = (avgs.get(i) != null) ? avgs.get(i) : 0.0;
                long avgLong = Math.round(avg * 100 * 100);
                item.addAverageLong(StocksEnum.of(stocks.get(i).getSymbol()), avgLong);
            }
            items.add(item);
        }

        items.sort((i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime()));

        // TODO aggregation to max points, if needed

        AllInOneChartDto dto = new AllInOneChartDto();
        dto.setItems(items);
        return dto;
    }

    private Map<LocalDate, List<Double>> avgPercentsPerDate(List<Stock> stocks, Optional<LocalDate> since) {

        // create several item lists after since date
        List<Map<LocalDate, Long>> avgPerDateList = new ArrayList<>();
        for (Stock stock : stocks) {
            Map<LocalDate, Long> avgPerDate = new HashMap<>();
            stock.getDayPrices().stream().
                    filter(dp -> !since.isPresent() || dp.getDay().isAfter(since.get())).
                    forEach(dp ->
                            avgPerDate.put(dp.getDay(), dp.getAverage())
                    );
            avgPerDateList.add(avgPerDate);
        }

        // find intersection dates
        Set<LocalDate> intersectionDates = new HashSet<>();
        avgPerDateList.get(0).keySet().stream().forEach(
                dayPrice -> intersectionDates.add(LocalDate.ofYearDay(dayPrice.getYear(), dayPrice.getDayOfYear())));
        avgPerDateList.stream().skip(1).forEach(items -> intersectionDates.retainAll(items.keySet()));

        if (intersectionDates.size() == 0)
            throw new RuntimeException("no intersection found");

        // find first averages, needed for calculating percentages
        LocalDate firstDate = intersectionDates.stream().min(LocalDate::compareTo).get();
        List<Long> firstAverages = new ArrayList<>();
        avgPerDateList.stream().forEach(avgPerDate -> firstAverages.add(avgPerDate.get(firstDate)));
        if (firstAverages.contains(null))
            throw new RuntimeException("missing first average for percentage calculation");

        Map<LocalDate, List<Double>> avgPercentsPerDate = new HashMap<>();
        for (LocalDate date : intersectionDates) {
            List<Double> averages = new ArrayList<>();
            for (int i = 0; i < avgPerDateList.size(); i++) {
                Long avg = avgPerDateList.get(i).get(date);
                if (avg == null) avg = 0L;
                long first = firstAverages.get(i);
                double avgPercent = 1.0 * (avg - first) / first;
                averages.add(avgPercent);
            }
            avgPercentsPerDate.put(date, averages);
        }
        return avgPercentsPerDate;
    }

    private void aggregate(List<ChartItemDTO> items, int points) {

        int index = 0;
        while (items.size() > points) {

            if (index + 1 < items.size()) {
                aggregate(items.get(index), items.get(index + 1));
                items.remove(index + 1);
                index += 1;
            } else {
                index = 0;
            }
        }
    }

    private void aggregate(ChartItemDTO item2Update, ChartItemDTO item2Merge) {
        item2Update.setMaxLong(max(item2Update.getMaxLong(), item2Merge.getMaxLong()));
        item2Update.setMinLong(min(item2Update.getMinLong(), item2Merge.getMinLong()));
        item2Update.setAverageLong(average(item2Update.getAverageLong(), item2Merge.getAverageLong()));

        long diff = item2Update.getDateTime().until(item2Merge.getDateTime(), ChronoUnit.SECONDS);
        LocalDateTime medium = item2Update.getDateTime().plus(diff / 2, ChronoUnit.SECONDS);
        item2Update.setDateTime(medium);
    }

    private Long average(Long l1, Long l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;
        return Math.round((l1 + l2) / 2.0);
    }

    private Long max(Long l1, Long l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;
        return Math.max(l1, l2);
    }

    private Long min(Long l1, Long l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;
        return Math.min(l1, l2);
    }

    private void transformIntoPercentages(List<ChartItemDTO> items) {
        if (items.isEmpty()) return;

        ChartItemDTO first = items.get(0);
        double firstAvg;
        if (first.getAverageLong() != null) {
            firstAvg = first.getAverageLong().doubleValue();
        } else if (first.getMinLong() != null && first.getMaxLong() != null) {
            firstAvg = (first.getMinLong().doubleValue() + first.getMaxLong().doubleValue()) / 2.0;
        } else {
            throw new RuntimeException("no first element for calculating percentages");
        }

        items.stream().forEach(item -> {
            item.setMinLong(percent(firstAvg, item.getMinLong()));
            item.setMaxLong(percent(firstAvg, item.getMaxLong()));
            item.setAverageLong(percent(firstAvg, item.getAverageLong()));
        });
    }

    private long percent(double first, Long value) {
        if (first == 0.0 || value == null || value == 0.0) {
            return 0;
        }
        double percent = (value.doubleValue() - first) / first;
        long percentLong = Math.round(percent * 100 * 100);
        return percentLong;
    }

}
