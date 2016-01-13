package de.mg.stock.server.util;

import javax.ejb.Local;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.Locale;

public class DateConverters {

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalTime localTime) {
        if (localTime == null) return null;
        Instant instant = Instant.from(localTime);
        return Date.from(instant);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        Instant instant = Instant.from(localDateTime.atZone(ZoneId.systemDefault()));
        return Date.from(instant);
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        Instant instantTime = date.toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        Instant instantTime = date.toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        Instant instantTime = date.toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalDateTime();
    }

    public static LocalDate toLocalDate(String s, String format) {
        return toLocalDate(s, format, null);
    }

    public static LocalDate toLocalDate(String s, String format, Locale locale) {
        SimpleDateFormat df;
        if (locale != null)
            df = new SimpleDateFormat(format, locale);
        else
            df = new SimpleDateFormat(format);
        try {
            return toLocalDate(df.parse(s));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDateTime toLocalDateTime(String s, String format, Locale locale) {
        SimpleDateFormat df;
        if (locale != null)
            df = new SimpleDateFormat(format, locale);
        else
            df = new SimpleDateFormat(format);
        try {
            return toLocalDateTime(df.parse(s));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
