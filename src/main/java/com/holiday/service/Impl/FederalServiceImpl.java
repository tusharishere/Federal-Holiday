package com.holiday.service.Impl;

import com.holiday.entity.FederalHoliday;
import com.holiday.payload.HolidayResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FederalServiceImpl {
    HolidayResponseDto getHolidaysByCountry(String countryCode);
    void deleteHolidaysByCountry(String countryCode);
    List<FederalHoliday> getHolidays();
    FederalHoliday addHoliday(String countryCode, String holidayName, String holidayDateStr);
    FederalHoliday updateHoliday(String countryCode, String holidayName, LocalDate holidayDate);
    void deleteHolidayByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
    ResponseEntity<Map<String, Object>> processMultipleCsvFiles(List<MultipartFile> files);
    FederalHoliday getHolidayByCountryAndDate(String countryCode, LocalDate holidayDate);
}
