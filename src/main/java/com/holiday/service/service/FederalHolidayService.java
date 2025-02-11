package com.holiday.service.service;

import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import com.holiday.exception.DuplicateHolidayException;
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

    public FederalHoliday getHolidayById(Long id) {
        return federalHolidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));
    }

    @Transactional
    public FederalHoliday addHoliday(String countryCode, String holidayName, LocalDate holidayDate) {
        Country country = countryRepository.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid country code"));

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


    @Transactional
    public FederalHoliday updateHoliday(Long id, String holidayName, LocalDate holidayDate) {
        FederalHoliday existingHoliday = federalHolidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found"));

        existingHoliday.setHolidayName(holidayName);
        existingHoliday.setHolidayDate(holidayDate);
        existingHoliday.setDayOfWeek(dateUtilService.calculateDayOfWeek(holidayDate));

        return federalHolidayRepository.save(existingHoliday);
    }

    @Transactional
    public void deleteHoliday(Long id) {
        if (!federalHolidayRepository.existsById(id)) {
            throw new IllegalArgumentException("Holiday not found");
        }
        federalHolidayRepository.deleteById(id);
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
