package com.holiday.utils;

import com.holiday.enums.DayOfWeekEnum;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class DateUtilService {
    public DayOfWeekEnum calculateDayOfWeek(LocalDate holidayDate) {
        if (holidayDate == null) {
            throw new IllegalArgumentException("Holiday date cannot be null");
        }
        DayOfWeek javaDayOfWeek = holidayDate.getDayOfWeek();
        return DayOfWeekEnum.valueOf(javaDayOfWeek.name());
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public LocalDate parseDate(String holidayDate) {
        if (holidayDate == null || holidayDate.isEmpty()) {
            throw new IllegalArgumentException("Holiday date cannot be null or empty");
        }
        return LocalDate.parse(holidayDate, FORMATTER);
    }

}
