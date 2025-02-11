package com.holiday.utils;

import com.holiday.enums.DayOfWeekEnum;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class DateUtilService {
    public DayOfWeekEnum calculateDayOfWeek(LocalDate holidayDate) {
        if (holidayDate == null) {
            throw new IllegalArgumentException("Holiday date cannot be null");
        }
        DayOfWeek javaDayOfWeek = holidayDate.getDayOfWeek();
        return DayOfWeekEnum.valueOf(javaDayOfWeek.name());
    }
}
