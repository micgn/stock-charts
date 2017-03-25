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

import de.mg.stock.dto.StockKeyDataDto
import de.mg.stock.dto.StocksEnum
import de.mg.stock.server.dao.StockDAO
import de.mg.stock.server.model.DayPrice
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.lang.Math.round
import java.time.LocalDate.now
import java.time.temporal.ChronoUnit.DAYS
import java.util.*
import javax.ejb.Singleton
import javax.inject.Inject

@Singleton
class KeyDataBuilder {

    private val DAY_INTERVALS = arrayOf(3, 7, 30, 90, 180, 365, 2 * 365, 3 * 365, 4 * 365, 5 * 365, 7 * 365,
            10 * 365, 15 * 365, 20 * 365, 30 * 365)

    @Inject
    lateinit private var stockDAO: StockDAO

    fun create(): List<StockKeyDataDto> {

        val result = ArrayList<StockKeyDataDto>()

        for (stock in stockDAO.findAllStocks()) {
            val all = stock.dayPrices

            val maxDistanceInDays = dayPricesInInterval(all, 50 * 365).map { it.day }.sorted().
                    map { it.until(now(), DAYS) }.first()

            for (distance in DAY_INTERVALS) {

                if (distance > maxDistanceInDays) continue

                val data = StockKeyDataDto()
                result.add(data)

                data.stock = StocksEnum.of(stock.symbol)
                data.distanceInDays = distance

                val min = dayPricesInInterval(all, distance).minBy { it.min }
                val max = dayPricesInInterval(all, distance).maxBy { it.max }

                data.min = min?.min
                data.minDate = min?.day

                data.max = max?.max
                data.maxDate = max?.day

                if (data.min != null && data.max != null && data.min != 0L) {
                    val minToMaxPercentage = data.max!!.toDouble() / data.min!!.toDouble() - 1.0
                    data.minToMaxPercentage = round(minToMaxPercentage * 100)
                }

                val first = dayPricesInInterval(all, distance).sortedBy { it.day }.firstOrNull()
                val last = dayPricesInInterval(all, distance).sortedBy { it.day }.reversed().firstOrNull()
                if (first != null && last != null) {
                    val percentage = last.average.toDouble() / first.average.toDouble() - 1.0
                    data.exactPerformancePercentage = round(percentage * 100)
                }

                if (distance >= 30) {
                    val lookAround = Math.min(distance / 4, 60)

                    val startStats = DescriptiveStatistics()
                    dayPricesInInterval(all, distance).sortedBy { it.day }.take(lookAround).forEach {
                        if (it.min != null) startStats.addValue(it.min.toDouble())
                        if (it.max != null) startStats.addValue(it.max.toDouble())
                    }

                    val endStats = DescriptiveStatistics()
                    dayPricesInInterval(all, distance).sortedBy { it.day }.reversed().take(lookAround).forEach {
                        if (it.min != null) endStats.addValue(it.min.toDouble())
                        if (it.max != null) endStats.addValue(it.max.toDouble())
                    }

                    val startPercentile = startStats.getPercentile(0.5)
                    val endPercentile = endStats.getPercentile(0.5)

                    val percentage = endPercentile / startPercentile - 1.0
                    data.averagePerformancePercentage = round(percentage * 100)
                }
            }
        }
        return result
    }

    private fun dayPricesInInterval(all: List<DayPrice>, distanceInDays: Int): List<DayPrice> {
        return all.filter { it.day.isAfter(now().minusDays(distanceInDays.toLong())) }
    }

}
