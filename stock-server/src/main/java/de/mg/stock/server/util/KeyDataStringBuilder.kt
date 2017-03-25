package de.mg.stock.server.util

import de.mg.stock.dto.StockKeyDataDto
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ofPattern
import javax.inject.Singleton

@Singleton
class KeyDataStringBuilder {

    fun asCsv(list: List<StockKeyDataDto>): String {

        val s = StringBuilder("stock;since;min date;min;max date;max;max-min;performance; average performance\n")
        for (data in list) {
            s.append(data.stock!!.toString()).append(";")
            s.append(since(data.distanceInDays!!)).append(";")
            s.append(date(data.minDate)).append(";")
            s.append(amount(data.min)).append(";")
            s.append(date(data.maxDate)).append(";")
            s.append(amount(data.max)).append(";")
            s.append(data.minToMaxPercentage).append("%").append("%;")
            s.append(data.exactPerformancePercentage).append("%").append(";")
            s.append(if (data.averagePerformancePercentage != null) data.averagePerformancePercentage?.toString() + "%" else "")
            s.append("\n")
        }
        return s.toString()
    }

    fun asHtml(list: List<StockKeyDataDto>) =
            "<table><tr><td>" + asCsv(list).dropLast(1).replace(";".toRegex(), "</td><td>").
                    replace("\n".toRegex(), "</td></tr><tr><td>") + "</td></tr></table>"


    private fun amount(l: Long?) =
            if (l == null) "" else DecimalFormat("#0.00").format(l / 100.0)


    private fun date(date: LocalDate?) =
            if (date == null) "" else date.format(ofPattern("dd.MM.yyyy"))


    private fun since(distance: Int) =
            now().minusDays(distance.toLong()).format(ofPattern("dd.MM.yyyy"))

}
