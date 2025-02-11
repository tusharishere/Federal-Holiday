package com.holiday.controller;

import com.holiday.entity.FederalHoliday;
import com.holiday.service.service.FederalHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;
import java.time.LocalDate;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class FederalHolidayController {

    private final FederalHolidayService federalHolidayService;


    @GetMapping("/{countryCode}")
    public ResponseEntity<Page<FederalHoliday>> getHolidaysByCountry(@PathVariable String countryCode, Pageable pageable) {
        return ResponseEntity.ok(federalHolidayService.getHolidaysByCountry(countryCode,pageable));
    }
    @GetMapping
    public ResponseEntity<List<FederalHoliday>> getHolidays() {
        List<FederalHoliday> holidays = federalHolidayService.getHolidays();
        return new ResponseEntity<>(holidays,HttpStatus.OK);
    }

    @DeleteMapping("/by-country/{countryCode}")
    public ResponseEntity<String> deleteHolidaysByCountry(@PathVariable String countryCode) {
        federalHolidayService.deleteHolidaysByCountry(countryCode);
        return new ResponseEntity<>("All the holidays have been deleted",HttpStatus.OK);
    }

    @GetMapping("/holiday/{id}")
    public ResponseEntity<FederalHoliday> getHolidayById(@PathVariable Long id) {
        FederalHoliday holidayById = federalHolidayService.getHolidayById(id);
        return new ResponseEntity<>(holidayById,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FederalHoliday> addHoliday(
            @RequestParam String countryCode,
            @RequestParam String holidayName,
            @RequestParam String holidayDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(holidayDate, formatter);
        FederalHoliday federalHoliday = federalHolidayService.addHoliday(countryCode, holidayName, date);
        return new ResponseEntity<>(federalHoliday,HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FederalHoliday> updateHoliday(
            @PathVariable Long id,
            @RequestParam String holidayName,
            @RequestParam String holidayDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(holidayDate, formatter);
        FederalHoliday federalHoliday = federalHolidayService.updateHoliday(id, holidayName, date);
        return new ResponseEntity<>(federalHoliday,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long id) {
        federalHolidayService.deleteHoliday(id);
        return new ResponseEntity<>("Holiday deleted successfully",HttpStatus.OK);
    }

    @PostMapping("/upload-csvs")
    public ResponseEntity<Map<String, Object>> uploadMultipleCsvFiles(@RequestParam("files") List<MultipartFile> files) {
        return federalHolidayService.processMultipleCsvFiles(files);
    }

}
