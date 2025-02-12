package com.holiday.payload;

import com.holiday.entity.FederalHoliday;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HolidayResponseDto {
    private List<FederalHoliday> holidays;
    private String message;
}
