package com.holiday.controller;

import com.holiday.entity.FederalHoliday;
import com.holiday.exception.InvalidHolidayDateException;
import com.holiday.payload.HolidayResponseDto;
import com.holiday.service.service.FederalHolidayService;
import com.holiday.utils.DateUtilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class FederalHolidayController {

    private final FederalHolidayService federalHolidayService;
    private final DateUtilService dateUtilService;


    @GetMapping(value = "/{countryCode}")
    public ResponseEntity<HolidayResponseDto> getHolidaysByCountry(@PathVariable String countryCode) {
        HolidayResponseDto holidaysByCountry = federalHolidayService.getHolidaysByCountry(countryCode);
        return new ResponseEntity<>(holidaysByCountry,HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<FederalHoliday>> getHolidays() {
        List<FederalHoliday> holidays = federalHolidayService.getHolidays();
        return new ResponseEntity<>(holidays,HttpStatus.OK);
    }

    @DeleteMapping(value = "/by-country/{countryCode}")
    public ResponseEntity<String> deleteHolidaysByCountry(@PathVariable String countryCode) {
        federalHolidayService.deleteHolidaysByCountry(countryCode);
        return new ResponseEntity<>("All the holidays have been deleted",HttpStatus.OK);
    }


    @GetMapping(value = "/by-country-date")
    public ResponseEntity<FederalHoliday> getHolidayByCountryAndDate(
            @RequestParam String countryCode,
            @RequestParam String holidayDate) {

        LocalDate localDate = dateUtilService.validateAndParseDate(holidayDate);
        FederalHoliday holiday = federalHolidayService.getHolidayByCountryAndDate(countryCode, localDate);
        return ResponseEntity.ok(holiday);
    }

    @PostMapping
    public ResponseEntity<FederalHoliday> addHoliday(@Valid
            @RequestParam String countryCode,
            @RequestParam String holidayName,
            @RequestParam String holidayDate) {

        FederalHoliday federalHoliday = federalHolidayService.addHoliday(countryCode, holidayName, holidayDate);
        return new ResponseEntity<>(federalHoliday,HttpStatus.CREATED);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<FederalHoliday> updateHoliday(
            @RequestParam String countryCode,
            @RequestParam String holidayDate,
            @RequestParam String holidayName) {

        LocalDate localDate = dateUtilService.validateAndParseDate(holidayDate);
        FederalHoliday federalHoliday = federalHolidayService.updateHoliday(countryCode, holidayName, localDate);
        return new ResponseEntity<>(federalHoliday, HttpStatus.OK);
    }

    @DeleteMapping(value = "/by-country-date")
    public ResponseEntity<String> deleteHolidayByCountryCodeAndHolidayDate(
                                                @RequestParam String countryCode,
                                                @RequestParam String holidayDate) {

        LocalDate localDate = dateUtilService.validateAndParseDate(holidayDate);
        federalHolidayService.deleteHolidayByCountryCodeAndHolidayDate(countryCode, localDate);
        return new ResponseEntity<>("Holiday deleted successfully",HttpStatus.OK);
    }
    
    @PostMapping(value = "/upload-csvs", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadMultipleCsvFiles(@RequestParam("files") List<MultipartFile> files) {
        Map<String, Object> result = federalHolidayService.processMultipleCsvFiles(files);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
