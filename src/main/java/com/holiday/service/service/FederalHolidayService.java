package com.holiday.service.service;

import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import com.holiday.exception.DuplicateHolidayException;
import com.holiday.exception.InvalidCountryCodeException;
import com.holiday.exception.InvalidHolidayDateException;
import com.holiday.exception.NoHolidaysFoundException;
import com.holiday.payload.HolidayResponseDto;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.FederalHolidayRepository;
import com.holiday.service.Impl.FederalServiceImpl;
import com.holiday.utils.DateUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FederalHolidayService implements FederalServiceImpl {

    private final FederalHolidayRepository federalHolidayRepository;
    private final CountryRepository countryRepository;
    private final DateUtilService dateUtilService;


    @Override
    public HolidayResponseDto getHolidaysByCountry(String countryCode) {

        Country country = validateCountryCode(countryCode);
        List<FederalHoliday> byCountryCountryCode = federalHolidayRepository.findByCountry_CountryCode(countryCode);

        if (byCountryCountryCode == null || byCountryCountryCode.isEmpty()) {
            return new HolidayResponseDto(null, "No holidays found for this country.");
        }

        return new HolidayResponseDto(byCountryCountryCode, null);
    }

    @Transactional
    public void deleteHolidaysByCountry(String countryCode) {

        Country country = validateCountryCode(countryCode);
        if (!federalHolidayRepository.existsByCountry_CountryCode(countryCode)) {
            throw new NoHolidaysFoundException("No holidays found for the country code: " + countryCode);
        }

        federalHolidayRepository.deleteByCountryCode(countryCode);
    }

    public List<FederalHoliday> getHolidays() {
        return federalHolidayRepository.findAll();
    }

    public FederalHoliday getHolidayByCountryAndDate(String countryCode, LocalDate holidayDate) {

        Country country = validateCountryCode(countryCode);
        FederalHoliday holiday = federalHolidayRepository
                .findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)
                .orElseThrow(() -> new NoHolidaysFoundException("Holiday not found for given country code and date"));
        return holiday;
    }

    @Transactional
    public FederalHoliday addHoliday(String countryCode, String holidayName, String holidayDateStr) {

        Country country = validateCountryCode(countryCode);
        LocalDate holidayDate = parseDate(holidayDateStr);

        if (federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(country, holidayName, holidayDate)) {
            throw new DuplicateHolidayException("Holiday with the same name and date already exists for this country.");
        }

        FederalHoliday holiday = new FederalHoliday();
        holiday.setCountry(country);
        holiday.setHolidayName(holidayName);
        holiday.setHolidayDate(holidayDate);
        holiday.setDayOfWeek(dateUtilService.calculateDayOfWeek(holidayDate));

        return federalHolidayRepository.save(holiday);
    }

    @Transactional
    public FederalHoliday updateHoliday(String countryCode, String holidayName, LocalDate holidayDate) {

        Country country = validateCountryCode(countryCode);
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

        Country country = validateCountryCode(countryCode);
        Country byCountryCode = countryRepository.findByCountryCode(countryCode);

        int deletedCount = federalHolidayRepository.deleteByCountryAndHolidayDate(byCountryCode, holidayDate);
        if (deletedCount == 0) {
            throw new NoHolidaysFoundException("No holiday found for the given country and date");
        }
    }

    public Map<String, Object> processMultipleCsvFiles(List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> fileResults = new ArrayList<>();

        for (MultipartFile file : files) {
            Map<String, Object> result = processSingleCsvFile(file);
            fileResults.add(Map.of("file_name", Objects.requireNonNull(file.getOriginalFilename()), "result", result));
        }

        response.put("files_processed", fileResults);
        return response;
    }

    public Map<String, Object> processSingleCsvFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "CSV file is empty.");
            return result;
        }

        List<FederalHoliday> addedRecords = new ArrayList<>();
        List<String> duplicateRows = new ArrayList<>();
        List<String> invalidRows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            Map<String, Integer> columnIndexMap = extractColumnIndexes(br);
            List<FederalHoliday> holidaysFromCSV = parseCsvLines(br, columnIndexMap, invalidRows);

            processHolidays(holidaysFromCSV, addedRecords, duplicateRows);

            result.put("added_records_count", addedRecords.size());
            result.put("duplicate_rows", duplicateRows);
            result.put("invalid_rows", invalidRows);

        } catch (Exception e) {
            result.put("error", "Error processing CSV file: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Integer> extractColumnIndexes(BufferedReader br) throws IOException {
        String headerLine = br.readLine();
        if (headerLine == null) {
            throw new IllegalArgumentException("CSV file is empty or missing headers.");
        }

        Map<String, Integer> columnIndexMap = new HashMap<>();
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            columnIndexMap.put(headers[i].trim().toLowerCase(), i);
        }

        if (!columnIndexMap.containsKey("country_code") ||
                !columnIndexMap.containsKey("holiday_name") ||
                !columnIndexMap.containsKey("holiday_date")) {
            throw new IllegalArgumentException("Missing required columns in CSV.");
        }

        return columnIndexMap;
    }

    private List<FederalHoliday> parseCsvLines(BufferedReader br, Map<String, Integer> columnIndexMap, List<String> invalidRows) throws IOException {
        List<FederalHoliday> holidaysFromCSV = new ArrayList<>();
        String line;
        int rowIndex = 1;

        while ((line = br.readLine()) != null) {
            rowIndex++;
            try {
                FederalHoliday holiday = parseCsvRow(line, columnIndexMap, rowIndex);
                holidaysFromCSV.add(holiday);
            } catch (IllegalArgumentException e) {
                invalidRows.add("Error in row " + rowIndex + ": " + line + " | Reason: " + e.getMessage());
            }
        }

        return holidaysFromCSV;
    }

    private FederalHoliday parseCsvRow(String line, Map<String, Integer> columnIndexMap, int rowIndex) {
        String[] data = line.split(",");

        String countryCode = data[columnIndexMap.get("country_code")].trim();
        String holidayName = data[columnIndexMap.get("holiday_name")].trim();
        String holidayDateStr = data[columnIndexMap.get("holiday_date")].trim();

        if (!countryCode.matches("00[1-3]")) {
            throw new IllegalArgumentException("Invalid Country code '" + countryCode + "' in row " + rowIndex + ".");
        }

        LocalDate holidayDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            holidayDate = LocalDate.parse(holidayDateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format in row " + rowIndex + ".");
        }

        Country country = countryRepository.findByCountryCode(countryCode);
        if (country == null) {
            throw new IllegalArgumentException("Country not found for code: " + countryCode + " in row " + rowIndex + ".");
        }

        FederalHoliday holiday = new FederalHoliday();
        holiday.setCountry(country);
        holiday.setHolidayDate(holidayDate);
        holiday.setHolidayName(holidayName);
        holiday.setDayOfWeek(dateUtilService.calculateDayOfWeek(holidayDate));

        return holiday;
    }

    private void processHolidays(List<FederalHoliday> holidaysFromCSV, List<FederalHoliday> addedRecords, List<String> duplicateRows) {
        for (FederalHoliday holiday : holidaysFromCSV) {
            if (federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(holiday.getCountry(), holiday.getHolidayName(), holiday.getHolidayDate())) {
                duplicateRows.add(formatHolidayRow(holiday));
            } else {
                federalHolidayRepository.save(holiday);
                addedRecords.add(holiday);
            }
        }
    }

    private String formatHolidayRow(FederalHoliday holiday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return String.join(",",
                holiday.getCountry().getCountryCode(),
                holiday.getHolidayName(),
                holiday.getHolidayDate().format(formatter));
    }

    private Country validateCountryCode(String countryCode) {
        if (!countryRepository.existsByCountryCode(countryCode)) {
            throw new InvalidCountryCodeException("Invalid country code: " + countryCode);
        }
        return countryRepository.findByCountryCode(countryCode);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return dateUtilService.parseDate(dateStr);
        } catch (InvalidHolidayDateException ex) {
            throw ex;
        } catch (DateTimeParseException ex) {
            String errorMessage = "Invalid date format. Please use dd-MMM-yyyy";
            if (ex.getMessage().contains("Invalid value")) {
                errorMessage = "Invalid date value. Check the day, month, and year.";
            }
            throw new InvalidHolidayDateException(errorMessage, ex);
        }
    }

}
