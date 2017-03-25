package de.mg.stock.server.util

import de.mg.stock.dto.StockKeyDataDto
import de.mg.stock.dto.StocksEnum
import org.junit.Test
import java.time.LocalDate


class KeyDataStringBuilderTest {

    @Test
    fun asHtml() {
        val sut = KeyDataStringBuilder();

        val d1 = StockKeyDataDto()
        d1.stock = StocksEnum.WORLD
        d1.averagePerformancePercentage = 1L
        d1.distanceInDays = 1
        d1.exactPerformancePercentage = 1L
        d1.max = 1L
        d1.maxDate = LocalDate.now()
        d1.min = 1L
        d1.minDate = LocalDate.now()
        d1.minToMaxPercentage = 1L

        val d2 = StockKeyDataDto()
        d2.stock = StocksEnum.WORLD
        d2.averagePerformancePercentage = 1L
        d2.distanceInDays = 1
        d2.exactPerformancePercentage = 1L
        d2.max = 1L
        d2.maxDate = LocalDate.now()
        d2.min = 1L
        d2.minDate = LocalDate.now()
        d2.minToMaxPercentage = 1L

        println(sut.asHtml(listOf(d1, d2)))
    }

}