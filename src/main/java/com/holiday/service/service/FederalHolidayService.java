package com.holiday.service.service;

import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import com.holiday.exception.DuplicateHolidayException;
import com.holiday.exception.InvalidHolidayDateException;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.FederalHolidayRepository;
import com.holiday.service.Impl.FederalServiceImpl;
import com.holiday.utils.DateUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FederalHolidayService implements FederalServiceImpl {

    private final FederalHolidayRepository federalHolidayRepository;
    private final CountryRepository countryRepository;
    private final DateUtilService dateUtilService;


    public Page<FederalHoliday> getHolidaysByCountry(String countryCode, Pageable pageable) {
        return federalHolidayRepository.findByCountry_CountryCode(countryCode,pageable);
    }
    @Transactional
    public void deleteHolidaysByCountry(String countryCode) {

        federalHolidayRepository.deleteByCountryCode(countryCode);
    }

    public List<FederalHoliday> getHolidays() {
        return federalHolidayRepository.findAll();
    }

    public FederalHoliday getHolidayByCountryAndDate(String countryCode, LocalDate holidayDate) {
        FederalHoliday holiday = federalHolidayRepository
                .findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found for given country code and date"));
        return holiday;
    }

    @Transactional
    public FederalHoliday addHoliday(String countryCode, String holidayName, LocalDate holidayDate) {
        Country country = countryRepository.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid country code"));


        if(federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(country, holidayName, holidayDate)){
            throw new DuplicateHolidayException("Holiday with the same name and date already exists for this country.");
        }

        FederalHoliday holiday = new FederalHoliday();
        holiday.setCountry(country);
        holiday.setHolidayName(holidayName);
        holiday.setHolidayDate(holidayDate);
        holiday.setDayOfWeek(dateUtilService.calculateDayOfWeek(holidayDate));

        try {
            return federalHolidayRepository.save(holiday);
        } catch (Exception e) {
            System.out.println("Error");
            throw new DuplicateHolidayException("Error: A holiday with the same name already exists for this country.");
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy"); // Your original format
            return LocalDate.parse(dateStr, formatter);
        } catch (java.time.format.DateTimeParseException ex) {
            String errorMessage = "Invalid date format. Please use dd-MMM-yyyy";
            if(ex.getMessage().contains("Invalid value")){
                errorMessage = "Invalid date value. Check the day, month, and year.";
            }
            throw new InvalidHolidayDateException(errorMessage);
        }
    }



    @Transactional
    public FederalHoliday updateHoliday(String countryCode, String holidayName, LocalDate holidayDate) {
        FederalHoliday existingHoliday = federalHolidayRepository
                .findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found for the given country code and date"));
        if (existingHoliday.getHolidayName().equalsIgnoreCase(holidayName)) {
            throw new IllegalArgumentException("No changes detected. Holiday details remain the same.");
        }
        existingHoliday.setHolidayName(holidayName);
        return federalHolidayRepository.save(existingHoliday);
    }

    @Transactional
    public void deleteHolidayByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate) {
        Country country = countryRepository.findByCountryCode(countryCode);
        if (country == null) {
            throw new IllegalArgumentException("Invalid country code");
        }

        int deletedCount = federalHolidayRepository.deleteByCountryAndHolidayDate(country, holidayDate);
        if (deletedCount == 0) {
            throw new IllegalArgumentException("No holiday found for the given country and date");
        }
    }

    public ResponseEntity<Map<String, Object>> processMultipleCsvFiles(List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> fileResults = new ArrayList<>();

        for (MultipartFile file : files) {
            Map<String, Object> result = processSingleCsvFile(file);
            fileResults.add(Map.of("file_name", Objects.requireNonNull(file.getOriginalFilename()), "result", result));
        }

        response.put("files_processed", fileResults);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> processSingleCsvFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "CSV file is empty.");
            return result;
        }

        List<FederalHoliday> holidayList = new ArrayList<>();
        List<String> errorRows = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            validateSingleCountryCode(br);

            String line;
            Map<String, Integer> columnIndexMap = new HashMap<>();
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (firstLine) {
                    for (int i = 0; i < data.length; i++) {
                        columnIndexMap.put(data[i].trim().toLowerCase(), i);
                    }
                    firstLine = false;
                    continue;
                }

                try {
                    if (!columnIndexMap.containsKey("country_code") ||
                            !columnIndexMap.containsKey("holiday_name") ||
                            !columnIndexMap.containsKey("holiday_date")) {
                        throw new IllegalArgumentException("Missing required columns in CSV.");
                    }

                    String countryCode = data[columnIndexMap.get("country_code")].trim();
                    String holidayName = data[columnIndexMap.get("holiday_name")].trim();
                    String holidayDateStr = data[columnIndexMap.get("holiday_date")].trim();

                    if (countryCode.isEmpty() || holidayName.isEmpty() || holidayDateStr.isEmpty()) {
                        throw new IllegalArgumentException("Empty values detected in required fields.");
                    }

                    LocalDate holidayDate = LocalDate.parse(holidayDateStr, formatter);

                    Country country = countryRepository.findByCountryCode(countryCode);
                    if (country == null) {
                        throw new IllegalArgumentException("Invalid country code: " + countryCode);
                    }

                    FederalHoliday holiday = new FederalHoliday();
                    holiday.setCountry(country);
                    holiday.setHolidayDate(holidayDate);
                    holiday.setHolidayName(holidayName);
                    holiday.setDayOfWeek(dateUtilService.calculateDayOfWeek(holidayDate));

                    try {
                        federalHolidayRepository.save(holiday);
                        holidayList.add(holiday);
                    } catch (Exception dbException) {
                        errorRows.add("Database error for row: " + line + " | Reason: " + dbException.getMessage());
                    }

                } catch (Exception e) {
                    errorRows.add("Error processing row: " + line + " | Reason: " + e.getMessage());
                }
            }

            result.put("uploaded_count", holidayList.size());
            result.put("failed_rows", errorRows);

        } catch (Exception e) {
            result.put("error", "Error processing CSV file: " + e.getMessage());
        }

        return result;
    }

    private void validateSingleCountryCode(BufferedReader br) throws IOException {
        Set<String> countryCodes = new HashSet<>();
        String line;
        boolean firstLine = true;
        Map<String, Integer> columnIndexMap = new HashMap<>();


        br.mark(10 * 1024); // Mark buffer limit (10KB)

        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");

            if (firstLine) {
                // Identify column indexes dynamically
                for (int i = 0; i < data.length; i++) {
                    columnIndexMap.put(data[i].trim().toLowerCase(), i);
                }

                if (!columnIndexMap.containsKey("country_code")) {
                    throw new IllegalArgumentException("CSV file is missing the required column: country_code");
                }

                firstLine = false;
                continue;
            }


            String countryCode = data[columnIndexMap.get("country_code")].trim();
            if (!countryCode.isEmpty()) {
                countryCodes.add(countryCode);
            }

            if (countryCodes.size() > 1) {
                throw new IllegalArgumentException("CSV file contains multiple country codes. Only one country is allowed per file.");
            }
        }


        br.reset();
    }

}
