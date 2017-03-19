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

import de.mg.stock.util.LocalDateTimeAdapter
import java.io.Serializable
import java.time.LocalDateTime
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

class ChartItemDTO(@get:XmlJavaTypeAdapter(LocalDateTimeAdapter::class) var dateTime: LocalDateTime,
                   var minLong: Long? = null,
                   var maxLong: Long? = null,
                   var averageLong: Long?,
                   var instantPrice: Boolean) : Serializable {

    val min: Double?
        get() = if (minLong != null) minLong!! / 100.0 else null

    val max: Double?
        get() = if (maxLong != null) maxLong!! / 100.0 else null

    val average: Double?
        get() = if (averageLong != null) averageLong!! / 100.0 else null

    val isValid: Boolean
        get() {
            var valid = true
            if (minLong != null && maxLong != null)
                valid = minLong!! <= maxLong!!
            if (minLong != null && averageLong != null)
                valid = valid && minLong!! <= averageLong!!
            if (averageLong != null && maxLong != null)
                valid = valid && averageLong!! <= maxLong!!
            return valid
        }

    override fun toString(): String {
        return "ChartItemDTO(dateTime=$dateTime, minLong=$minLong, maxLong=$maxLong, averageLong=$averageLong, instantPrice=$instantPrice)"
    }

}
