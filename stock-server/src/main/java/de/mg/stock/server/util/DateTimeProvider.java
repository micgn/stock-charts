package de.mg.stock.server.util;

import javax.ejb.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Singleton
public class DateTimeProvider {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public LocalDate today() {
        return LocalDate.now();
    }
}
