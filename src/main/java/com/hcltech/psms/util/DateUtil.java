package com.hcltech.psms.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static LocalDate parseDMY(String s) {
        return LocalDate.parse(s, DMY);
    }
    public static String formatDMY(LocalDate d) {
        return d == null ? "" : d.format(DMY);
    }

}
