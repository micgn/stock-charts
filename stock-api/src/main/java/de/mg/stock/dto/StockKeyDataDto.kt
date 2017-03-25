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
package de.mg.stock.dto

import de.mg.stock.util.LocalDateAdapter
import java.time.LocalDate
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter


@XmlRootElement
class StockKeyDataDto {

    var stock: StocksEnum? = null

    var distanceInDays: Int? = null

    var min: Long? = null

    var max: Long? = null

    var minToMaxPercentage: Long? = null

    @get:XmlJavaTypeAdapter(LocalDateAdapter::class)
    var minDate: LocalDate? = null

    @get:XmlJavaTypeAdapter(LocalDateAdapter::class)
    var maxDate: LocalDate? = null

    var exactPerformancePercentage: Long? = null

    var averagePerformancePercentage: Long? = null

    override fun toString(): String {
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
                '}'
    }
}
