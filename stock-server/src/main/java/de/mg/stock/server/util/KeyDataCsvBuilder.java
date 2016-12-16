package de.mg.stock.server.util;

import de.mg.stock.dto.StockKeyDataDto;

import javax.inject.Singleton;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Singleton
public class KeyDataCsvBuilder {

    public String asCsv(List<StockKeyDataDto> list) {

        StringBuilder s = new StringBuilder("stock;distance in days;min date;min;max date;max;min to max %;performance %; average performance %\n");
        for (StockKeyDataDto data : list) {
            s.append(data.getStock().toString()).append(";");
            s.append(data.getDistanceInDays()).append(";");
            s.append(date(data.getMinDate())).append(";");
            s.append(amount(data.getMin())).append(";");
            s.append(date(data.getMaxDate())).append(";");
            s.append(amount(data.getMax())).append(";");
            s.append(data.getMinToMaxPercentage()).append(";");
            s.append(data.getExactPerformancePercentage()).append(";");
            s.append(data.getAveragePerformancePercentage());
            s.append("\n");
        }
        return s.toString();
    }

    private String amount(Long l) {
        if (l == null) return "";
        return new DecimalFormat("#0.00").format(l / 100.0);
    }

    private String date(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
