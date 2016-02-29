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

package de.mg.stock.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.Locale;

public final class DateConverters {

    private DateConverters() {
    }

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
        Instant instantTime = toUtilDate(date).toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        if (date == null) return null;
        Instant instantTime = toUtilDate(date).toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        Instant instantTime = toUtilDate(date).toInstant();
        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
        return zdt.toLocalDateTime();
    }

    // java.sql.Date does not implement toInstant()
    private static Date toUtilDate(Date date) {
        return (date != null) ? new Date(date.getTime()) : null;
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
