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
    lateinit private var dateTimeProvider: DateTimeProvider

    fun createOne(stock: Stock, points: Int, sinceParam: Optional<LocalDate>, percentages: Boolean): ChartDataDTO {
        val since: LocalDate? = sinceParam.orElse(null)

        val dto = ChartDataDTO(stock.name, dateTimeProvider.now())

        val dayPoints = if (stock.instantPrices.isNotEmpty()) (points * 0.8).toInt() else points
        val instantPoints = if (stock.instantPrices.isNotEmpty()) (points * 0.2).toInt() else 0

        val firstInstantPrice = stock.instantPrices.minBy { it.time }?.time ?: LocalDateTime.MAX

        val isAfterSinceAndBeforeInstantPrices = { dp: DayPrice ->
            val isAfterSince = since == null || dp.day.isAfter(since) || dp.day.isEqual(since)
            isAfterSince && dp.day.plus(1, ChronoUnit.DAYS).atStartOfDay().isBefore(firstInstantPrice)
        }

        val dayItems = stock.dayPrices.filter(isAfterSinceAndBeforeInstantPrices).sortedBy { it.day }.map { dp ->
            ChartItemDTO(dateTime = dp.day.atStartOfDay(), minLong = dp.min, maxLong = dp.max,
                    averageLong = average(dp.min, dp.max), instantPrice = false)
        }.toMutableList()

        aggregate(dayItems, dayPoints)

        val instantItems = stock.instantPrices.sortedBy { it.time }.map { ip ->
            ChartItemDTO(dateTime = ip.time, minLong = ip.min, maxLong = ip.max,
                    averageLong = average(ip.min, ip.max), instantPrice = true)
        }.toMutableList()

        aggregate(instantItems, instantPoints)

        dto.items.addAll(dayItems)
        dto.items.addAll(instantItems)

        if (percentages) transformToPercentages(dto.items)

        return dto
    }


    fun createAggregated(stocks: List<Stock>, stockWeights: List<Int>, points: Int, sinceParam: Optional<LocalDate>): ChartDataDTO {
        val since: LocalDate? = sinceParam.orElse(null)

        if (stocks.size < 2) throw RuntimeException("at least 2 stocks need to be aggregated")
        if (stocks.size != stockWeights.size) throw RuntimeException("different amounts of stocks and stock weights")

        val avgPercentsPerDate: Map<LocalDate, List<Double>> = avgPercentsPerDate(stocks, since)

        val aggregatedItemList =
                avgPercentsPerDate.keys.map { date ->

                    val averagePercents = avgPercentsPerDate[date] ?: listOf()
                    val aggregatedAvgPercent = averagePercents.mapIndexed { index, percent -> percent * stockWeights[index] / 100.0 }.sum()
                    val aggregatedAvgPercentLong = round(aggregatedAvgPercent * 100.0 * 100.0)

                    ChartItemDTO(dateTime = date.atStartOfDay(), averageLong = aggregatedAvgPercentLong, instantPrice = false)

                }.sortedBy { it.dateTime }.toMutableList()

        aggregate(aggregatedItemList, points)

        val dto = ChartDataDTO("aggregated", dateTimeProvider.now())
        dto.items.addAll(aggregatedItemList)
        return dto
    }


    fun createAllInOne(stocks: List<Stock>, points: Int, since: LocalDate): AllInOneChartDto {

        val avgPercentsPerDate = avgPercentsPerDate(stocks, since)

        val items = avgPercentsPerDate.keys.map { date ->

            val averagePercents = avgPercentsPerDate[date] ?: listOf()

            val item = AllInOneChartItemDto()
            item.dateTime = date.atStartOfDay()
            for ((index, avg) in averagePercents.withIndex()) {
                val avgLong = round(avg * 100.0 * 100.0)
                val symbol = StocksEnum.of(stocks[index].symbol)
                item.addAverageLong(symbol, avgLong)
            }
            item
        }.sortedBy { it.dateTime }.toMutableList()

        // TODO aggregation to max points, if needed

        val dto = AllInOneChartDto()
        dto.items = items
        return dto
    }


    private fun avgPercentsPerDate(stocks: List<Stock>, since: LocalDate?): Map<LocalDate, List<Double>> {

        // create several item lists after since date
        val avgPerDateList = stocks.map {
            it.dayPrices.
                    filter { dp -> since == null || dp.day.isAfter(since) }.
                    map { dp -> dp.day to dp.average }.toMap()
        }

        // find intersection dates
        val intersectionDates = mutableSetOf<LocalDate>()
        avgPerDateList[0].keys.forEach {
            dayPrice ->
            intersectionDates.add(LocalDate.ofYearDay(dayPrice.year, dayPrice.dayOfYear))
        }
        avgPerDateList.forEachIndexed {
            index, items ->
            if (index != 0) intersectionDates.retainAll(items.keys)
        }

        if (intersectionDates.size == 0) throw RuntimeException("no intersection found")

        // find first averages, needed for calculating percentages
        val firstDate = intersectionDates.min()
        val firstAverages = avgPerDateList.map { avgPerDate -> avgPerDate[firstDate] }

        if (firstAverages.contains(null)) throw RuntimeException("missing first average for percentage calculation")


        val avgPercentsPerDate = intersectionDates.map { date ->
            val averages = avgPerDateList.mapIndexed { index, avgPerDate ->
                val avg = avgPerDate[date] ?: 0
                val first = firstAverages[index] ?: 0
                val avgPercent = 1.0 * (avg - first) / first
                avgPercent
            }
            date to averages
        }.toMap()

        return avgPercentsPerDate
    }


    private fun aggregate(items: MutableList<ChartItemDTO>, pointsNeeded: Int): Unit {

        fun aggregate(updateItem: ChartItemDTO, mergeItem: ChartItemDTO): Unit {
            updateItem.maxLong = max(updateItem.maxLong, mergeItem.maxLong)
            updateItem.minLong = min(updateItem.minLong, mergeItem.minLong)
            updateItem.averageLong = average(updateItem.averageLong, mergeItem.averageLong)

            val diff = updateItem.dateTime.until(mergeItem.dateTime, ChronoUnit.SECONDS)
            val medium = updateItem.dateTime.plus(diff / 2, ChronoUnit.SECONDS)
            updateItem.dateTime = medium
        }

        var index = 0
        while (items.size > pointsNeeded) {
            if (index + 1 < items.size) {
                aggregate(items[index], items[index + 1])
                items.removeAt(index + 1)
                index += 1
            } else {
                index = 0
            }
        }
    }


    private fun transformToPercentages(items: List<ChartItemDTO>) {
        if (items.isEmpty()) return

        val first = items[0]
        val firstAvg: Double =
                if (first.averageLong != null)
                    first.averageLong!!.toDouble()
                else if (first.minLong != null && first.maxLong != null)
                    (first.minLong!! + first.maxLong!!) / 2.0
                else throw RuntimeException("no first element for calculating percentages")

        items.forEach { item ->
            item.minLong = percent(firstAvg, item.minLong)
            item.maxLong = percent(firstAvg, item.maxLong)
            item.averageLong = percent(firstAvg, item.averageLong)
        }
    }

}

private fun percent(first: Double, value: Long?): Long =
        if (first == 0.0 || value == null || value == 0L) 0
        else {
            val percent = (value.toDouble() - first) / first
            round(percent * 100.0 * 100.0)
        }


private fun average(l1: Long?, l2: Long?): Long? =
        if (l1 == null) l2 else if (l2 == null) l1 else round((l1 + l2) / 2.0)


private fun max(l1: Long?, l2: Long?): Long? =
        if (l1 == null) l2 else if (l2 == null) l1 else Math.max(l1, l2)


private fun min(l1: Long?, l2: Long?): Long? =
        if (l1 == null) l2 else if (l2 == null) l1 else Math.min(l1, l2)

