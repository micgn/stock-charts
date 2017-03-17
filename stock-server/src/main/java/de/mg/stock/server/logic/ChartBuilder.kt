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

package de.mg.stock.server.logic

import de.mg.stock.dto.*
import de.mg.stock.server.model.DayPrice
import de.mg.stock.server.model.Stock
import de.mg.stock.server.util.DateTimeProvider
import java.lang.Math.round
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.ejb.Singleton
import javax.inject.Inject

@Singleton
class ChartBuilder {

    @Inject
    private var dateTimeProvider: DateTimeProvider? = null

    fun createOne(stock: Stock, points: Int, since: Optional<LocalDate>, percentages: Boolean): ChartDataDTO {

        val dto = ChartDataDTO(stock.name, dateTimeProvider!!.now())

        val dayPoints: Int
        val instantPoints: Int
        if (stock.instantPrices.size > 0) {
            dayPoints = (points * 0.8).toInt()
            instantPoints = (points * 0.2).toInt()
        } else {
            dayPoints = points
            instantPoints = 0
        }

        val firstInstantPrice = stock.instantPrices.minBy { it.time }?.time ?: LocalDateTime.MAX

        val isAfterSinceAndBeforeInstantPrices = { dp: DayPrice ->
            val isAfterSince = if (!since.isPresent)
                true
            else
                dp.day.isAfter(since.get()) || dp.day.isEqual(since.get())
            isAfterSince && dp.day.plus(1, ChronoUnit.DAYS).atStartOfDay().isBefore(firstInstantPrice)
        }

        val dayItems = ArrayList<ChartItemDTO>()
        stock.dayPrices.filter(isAfterSinceAndBeforeInstantPrices).sortedBy { it.day }.forEach {
            dp ->
            dayItems.add(ChartItemDTO.Builder().setDateTime(dp.day.atStartOfDay()).setMinLong(dp.min).
                    setMaxLong(dp.max).setAverageLong(average(dp.min, dp.max)).setInstantPrice(false).build())
        }
        aggregate(dayItems, dayPoints)

        val instantItems = ArrayList<ChartItemDTO>()
        stock.instantPrices.sortedBy { it.time }.forEach {
            ip ->
            instantItems.add(ChartItemDTO.Builder().setDateTime(ip.time).setMinLong(ip.min).setMaxLong(ip.max).
                    setAverageLong(average(ip.min, ip.max)).setInstantPrice(true).build())
        }
        aggregate(instantItems, instantPoints)

        dto.items.addAll(dayItems)
        dto.items.addAll(instantItems)

        if (percentages)
            transformIntoPercentages(dto.items)

        return dto
    }

    fun createAggregated(stocks: List<Stock>, stockWeights: List<Int>, points: Int, since: Optional<LocalDate>): ChartDataDTO {

        if (stocks.size < 2)
            throw RuntimeException("at least 2 stocks need to be aggregated")
        if (stocks.size != stockWeights.size)
            throw RuntimeException("different amounts of stocks and stock weights")

        val avgPercentsPerDate = avgPercentsPerDate(stocks, since)

        val aggregatedItemList = ArrayList<ChartItemDTO>()
        for (date in avgPercentsPerDate.keys) {
            val avgPercents = avgPercentsPerDate[date]
            var aggregatedAvgPercent = 0.0
            for (i in avgPercents!!.indices) {
                val avg = avgPercents!!.get(i)
                aggregatedAvgPercent += avg * stockWeights[i] / 100.0
            }

            val aggregatedAvgPercentLong = round(aggregatedAvgPercent * 100.0 * 100.0)
            val itemDto = ChartItemDTO.Builder().setDateTime(date.atStartOfDay()).
                    setAverageLong(aggregatedAvgPercentLong).setInstantPrice(false).build()
            aggregatedItemList.add(itemDto)
        }

        val sortedAggregatedItemList = aggregatedItemList.sortedBy { it.dateTime }
        aggregatedItemList.sortBy { it.dateTime }
        aggregate(aggregatedItemList, points)

        val dto = ChartDataDTO("aggregated", dateTimeProvider!!.now())
        dto.items.addAll(aggregatedItemList)
        return dto
    }

    fun createAllInOne(stocks: List<Stock>, points: Int, since: LocalDate): AllInOneChartDto {

        val avgPercentsPerDate = avgPercentsPerDate(stocks, Optional.of(since))

        val items = ArrayList<AllInOneChartItemDto>()
        for (date in avgPercentsPerDate.keys) {
            val avgs = avgPercentsPerDate[date]

            val item = AllInOneChartItemDto()
            item.dateTime = date.atStartOfDay()
            for (i in avgs!!.indices) {
                val avg = if (avgs!!.get(i) != null) avgs.get(i) else 0.0
                val avgLong = round(avg * 100.0 * 100.0)
                item.addAverageLong(StocksEnum.of(stocks[i].symbol), avgLong)
            }
            items.add(item)
        }

        items.sortBy { it.dateTime }

        // TODO aggregation to max points, if needed

        val dto = AllInOneChartDto()
        dto.items = items
        return dto
    }

    private fun avgPercentsPerDate(stocks: List<Stock>, since: Optional<LocalDate>): Map<LocalDate, List<Double>> {

        // create several item lists after since date
        val avgPerDateList = ArrayList<Map<LocalDate, Long>>()
        for (stock in stocks) {
            val avgPerDate = HashMap<LocalDate, Long>()
            stock.dayPrices.stream().filter { dp -> !since.isPresent || dp.day.isAfter(since.get()) }.forEach { dp -> avgPerDate.put(dp.day, dp.average) }
            avgPerDateList.add(avgPerDate)
        }

        // find intersection dates
        val intersectionDates = HashSet<LocalDate>()
        avgPerDateList[0].keys.stream().forEach { dayPrice -> intersectionDates.add(LocalDate.ofYearDay(dayPrice.year, dayPrice.dayOfYear)) }
        avgPerDateList.stream().skip(1).forEach { items -> intersectionDates.retainAll(items.keys) }

        if (intersectionDates.size == 0)
            throw RuntimeException("no intersection found")

        // find first averages, needed for calculating percentages
        val firstDate = intersectionDates.stream().min { obj, other -> obj.compareTo(other) }.get()
        val firstAverages = ArrayList<Long?>()
        avgPerDateList.stream().forEach { avgPerDate -> firstAverages.add(avgPerDate[firstDate]) }
        if (firstAverages.contains(null))
            throw RuntimeException("missing first average for percentage calculation")

        val avgPercentsPerDate = HashMap<LocalDate, List<Double>>()
        for (date in intersectionDates) {
            val averages = ArrayList<Double>()
            for (i in avgPerDateList.indices) {
                val avg: Long = avgPerDateList[i][date] ?: 0L
                val first = firstAverages[i] ?: 0L
                val avgPercent = 1.0 * (avg - first) / first
                averages.add(avgPercent)
            }
            avgPercentsPerDate.put(date, averages)
        }
        return avgPercentsPerDate
    }

    private fun aggregate(items: MutableList<ChartItemDTO>, points: Int) {

        var index = 0
        while (items.size > points) {

            if (index + 1 < items.size) {
                aggregate(items[index], items[index + 1])
                items.removeAt(index + 1)
                index += 1
            } else {
                index = 0
            }
        }
    }

    private fun aggregate(item2Update: ChartItemDTO, item2Merge: ChartItemDTO) {
        item2Update.maxLong = max(item2Update.maxLong, item2Merge.maxLong)
        item2Update.minLong = min(item2Update.minLong, item2Merge.minLong)
        item2Update.averageLong = average(item2Update.averageLong, item2Merge.averageLong)

        val diff = item2Update.dateTime.until(item2Merge.dateTime, ChronoUnit.SECONDS)
        val medium = item2Update.dateTime.plus(diff / 2, ChronoUnit.SECONDS)
        item2Update.dateTime = medium
    }

    private fun average(l1: Long?, l2: Long?): Long? {
        if (l1 == null) return l2
        if (l2 == null) return l1
        return round((l1 + l2) / 2.0)
    }

    private fun max(l1: Long?, l2: Long?): Long? {
        if (l1 == null) return l2
        if (l2 == null) return l1
        return Math.max(l1, l2)
    }

    private fun min(l1: Long?, l2: Long?): Long? {
        if (l1 == null) return l2
        if (l2 == null) return l1
        return Math.min(l1, l2)
    }

    private fun transformIntoPercentages(items: List<ChartItemDTO>) {
        if (items.isEmpty()) return

        val first = items[0]
        val firstAvg: Double
        if (first.averageLong != null) {
            firstAvg = first.averageLong!!.toDouble()
        } else if (first.minLong != null && first.maxLong != null) {
            firstAvg = (first.minLong!!.toDouble() + first.maxLong!!.toDouble()) / 2.0
        } else {
            throw RuntimeException("no first element for calculating percentages")
        }

        items.stream().forEach { item ->
            item.minLong = percent(firstAvg, item.minLong)
            item.maxLong = percent(firstAvg, item.maxLong)
            item.averageLong = percent(firstAvg, item.averageLong)
        }
    }

    private fun percent(first: Double, value: Long?): Long {
        if (first == 0.0 || value == null || value == 0L) {
            return 0
        }
        val percent = (value.toDouble() - first) / first
        val percentLong = round(percent * 100.0 * 100.0)
        return percentLong
    }

}
