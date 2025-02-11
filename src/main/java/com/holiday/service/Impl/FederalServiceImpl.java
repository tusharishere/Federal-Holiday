package com.holiday.service.Impl;

import com.holiday.entity.FederalHoliday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FederalServiceImpl {
    Page<FederalHoliday> getHolidaysByCountry(String countryCode, Pageable pageable);
    void deleteHolidaysByCountry(String countryCode);
    List<FederalHoliday> getHolidays();
    FederalHoliday addHoliday(String countryCode, String holidayName, LocalDate holidayDate);
    FederalHoliday updateHoliday(String countryCode, String holidayName, LocalDate holidayDate);
    void deleteHolidayByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
    ResponseEntity<Map<String, Object>> processMultipleCsvFiles(List<MultipartFile> files);
    FederalHoliday getHolidayByCountryAndDate(String countryCode, LocalDate holidayDate);
}
