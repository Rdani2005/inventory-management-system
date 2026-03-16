package edu.inventory.administrator.ui.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Formatters {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Formatters() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    public static String text(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
