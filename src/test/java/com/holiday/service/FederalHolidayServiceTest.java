package com.holiday.service;

import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import com.holiday.enums.DayOfWeekEnum;
import com.holiday.exception.DuplicateHolidayException;
import com.holiday.exception.InvalidCountryCodeException;
import com.holiday.exception.InvalidHolidayDateException;
import com.holiday.exception.NoHolidaysFoundException;
import com.holiday.payload.HolidayResponseDto;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.FederalHolidayRepository;
import com.holiday.service.service.FederalHolidayService;
import com.holiday.utils.DateUtilService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FederalHolidayServiceTest {

    @Mock
    private FederalHolidayRepository federalHolidayRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private DateUtilService dateUtilService;

    @InjectMocks
    private FederalHolidayService federalHolidayService;

    @Test
    void getHolidaysByCountry_validCountryCode_returnsHolidays() {
        String countryCode = "USA";
        Country country = new Country();
        country.setCountryCode(countryCode);
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        List<FederalHoliday> holidays = List.of(new FederalHoliday());
        when(federalHolidayRepository.findByCountry_CountryCode(countryCode)).thenReturn(holidays);

        HolidayResponseDto response = federalHolidayService.getHolidaysByCountry(countryCode);

        assertNotNull(response);
        assertEquals(holidays, response.getHolidays());
        assertNull(response.getMessage());
    }

    @Test
    void getHolidaysByCountry_invalidCountryCode_throwsInvalidCountryCodeException() {
        String countryCode = "XYZ";
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(false);

        assertThrows(InvalidCountryCodeException.class, () -> federalHolidayService.getHolidaysByCountry(countryCode));
    }

    @Test
    void getHolidaysByCountry_noHolidaysFound_returnsEmptyListAndMessage() {
        String countryCode = "USA";
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.findByCountry_CountryCode(countryCode)).thenReturn(List.of()); // Empty list

        HolidayResponseDto response = federalHolidayService.getHolidaysByCountry(countryCode);

        assertNotNull(response);
        assertNotNull(response.getHolidays()); // Check if the list itself is not null
        assertTrue(response.getHolidays().isEmpty()); // Now it's safe to check isEmpty()
        assertEquals("No holidays found for this country.", response.getMessage());
    }


    @Test
    void deleteHolidaysByCountry_validCountryCode_deletesHolidays() {
        String countryCode = "USA";
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.existsByCountry_CountryCode(countryCode)).thenReturn(true);

        federalHolidayService.deleteHolidaysByCountry(countryCode);

        verify(federalHolidayRepository).deleteByCountryCode(countryCode);
    }

    @Test
    void deleteHolidaysByCountry_invalidCountryCode_throwsInvalidCountryCodeException() {
        String countryCode = "XYZ";
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(false);

        assertThrows(InvalidCountryCodeException.class, () -> federalHolidayService.deleteHolidaysByCountry(countryCode));
    }

    @Test
    void deleteHolidaysByCountry_noHolidaysFound_throwsNoHolidaysFoundException() {
        String countryCode = "USA";
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.existsByCountry_CountryCode(countryCode)).thenReturn(false);

        assertThrows(NoHolidaysFoundException.class, () -> federalHolidayService.deleteHolidaysByCountry(countryCode));
    }


    @Test
    void getHolidays_returnsAllHolidays() {
        List<FederalHoliday> holidays = List.of(new FederalHoliday());
        when(federalHolidayRepository.findAll()).thenReturn(holidays);

        List<FederalHoliday> result = federalHolidayService.getHolidays();

        assertEquals(holidays, result);
    }


    @Test
    void getHolidayByCountryAndDate_validInput_returnsHoliday() {
        String countryCode = "USA";
        LocalDate date = LocalDate.now();
        Country country = new Country();
        country.setCountryCode(countryCode);
        FederalHoliday holiday = new FederalHoliday();
        holiday.setCountry(country);
        holiday.setHolidayDate(date);

        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.findByCountry_CountryCodeAndHolidayDate(countryCode, date)).thenReturn(Optional.of(holiday));

        FederalHoliday result = federalHolidayService.getHolidayByCountryAndDate(countryCode, date);

        assertEquals(holiday, result);
    }

    @Test
    void getHolidayByCountryAndDate_invalidCountryCode_throwsInvalidCountryCodeException() {
        String countryCode = "XYZ";
        LocalDate date = LocalDate.now();
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(false);

        assertThrows(InvalidCountryCodeException.class, () -> federalHolidayService.getHolidayByCountryAndDate(countryCode, date));
    }

    @Test
    void getHolidayByCountryAndDate_holidayNotFound_throwsIllegalArgumentException() {
        String countryCode = "USA";
        LocalDate date = LocalDate.now();
        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.findByCountry_CountryCodeAndHolidayDate(countryCode, date)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.getHolidayByCountryAndDate(countryCode, date));
    }


    @Test
    void addHoliday_validInput_addsHoliday() {
        String countryCode = "USA";
        String holidayName = "Test Holiday";
        String holidayDateStr = "01-Jan-2024";
        LocalDate holidayDate = LocalDate.parse(holidayDateStr, DateTimeFormatter.ofPattern("dd-MMM-yyyy", java.util.Locale.ENGLISH));
        Country country = new Country();
        country.setCountryCode(countryCode);
        FederalHoliday holiday = new FederalHoliday();
        holiday.setCountry(country);
        holiday.setHolidayName(holidayName);
        holiday.setHolidayDate(holidayDate);
        holiday.setDayOfWeek(DayOfWeekEnum.MONDAY);

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(dateUtilService.parseDate(holidayDateStr)).thenReturn(holidayDate);
        when(federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(country, holidayName, holidayDate)).thenReturn(false);
        when(dateUtilService.calculateDayOfWeek(holidayDate)).thenReturn(DayOfWeekEnum.MONDAY);
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(holiday);

        FederalHoliday result = federalHolidayService.addHoliday(countryCode, holidayName, holidayDateStr);

        assertNotNull(result);
        assertEquals(holidayName, result.getHolidayName());
        assertEquals(holidayDate, result.getHolidayDate());
        assertEquals(DayOfWeekEnum.MONDAY, result.getDayOfWeek());
        verify(federalHolidayRepository).save(any(FederalHoliday.class));
    }

    @Test
    void addHoliday_duplicateHoliday_throwsDuplicateHolidayException() {
        String countryCode = "USA";
        String holidayName = "Test Holiday";
        String holidayDateStr = "01-Jan-2024";
        LocalDate holidayDate = LocalDate.parse(holidayDateStr, DateTimeFormatter.ofPattern("dd-MMM-yyyy", java.util.Locale.ENGLISH));
        Country country = new Country();
        country.setCountryCode(countryCode);

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(dateUtilService.parseDate(holidayDateStr)).thenReturn(holidayDate);
        when(federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(country, holidayName, holidayDate)).thenReturn(true);

        assertThrows(DuplicateHolidayException.class, () -> federalHolidayService.addHoliday(countryCode, holidayName, holidayDateStr));
    }

    @Test
    void addHoliday_invalidCountryCode_throwsIllegalArgumentException() {
        String countryCode = "XYZ";
        String holidayName = "Test Holiday";
        String holidayDateStr = "01-Jan-2024";

        when(countryRepository.findById(countryCode)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.addHoliday(countryCode, holidayName, holidayDateStr));
    }

    @Test
    void addHoliday_invalidDate_throwsInvalidHolidayDateException() {
        String countryCode = "USA";
        String holidayName = "Test Holiday";
        String holidayDateStr = "invalid date";
        Country country = new Country();
        country.setCountryCode(countryCode);

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        doThrow(new InvalidHolidayDateException("Invalid holiday date: " + holidayDateStr)).when(dateUtilService).parseDate(holidayDateStr);

        assertThrows(InvalidHolidayDateException.class, () -> federalHolidayService.addHoliday(countryCode, holidayName, holidayDateStr));
    }

    @Test
    void updateHoliday_validInput_updatesHoliday() {
        String countryCode = "USA";
        String holidayName = "Updated Holiday";
        LocalDate holidayDate = LocalDate.now();
        FederalHoliday existingHoliday = new FederalHoliday();
        existingHoliday.setCountry(new Country());
        existingHoliday.setHolidayName("Original Holiday");
        existingHoliday.setHolidayDate(holidayDate);

        when(federalHolidayRepository.findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)).thenReturn(Optional.of(existingHoliday));
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(existingHoliday);

        FederalHoliday result = federalHolidayService.updateHoliday(countryCode, holidayName, holidayDate);

        assertNotNull(result);
        assertEquals(holidayName, result.getHolidayName());
        verify(federalHolidayRepository).save(any(FederalHoliday.class));
    }

    @Test
    void updateHoliday_noChanges_throwsIllegalArgumentException() {
        String countryCode = "USA";
        String holidayName = "Original Holiday";
        LocalDate holidayDate = LocalDate.now();
        FederalHoliday existingHoliday = new FederalHoliday();
        existingHoliday.setCountry(new Country());
        existingHoliday.setHolidayName(holidayName);
        existingHoliday.setHolidayDate(holidayDate);

        when(federalHolidayRepository.findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)).thenReturn(Optional.of(existingHoliday));

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.updateHoliday(countryCode, holidayName, holidayDate));
    }

    @Test
    void updateHoliday_holidayNotFound_throwsIllegalArgumentException() {
        String countryCode = "USA";
        String holidayName = "Updated Holiday";
        LocalDate holidayDate = LocalDate.now();

        when(federalHolidayRepository.findByCountry_CountryCodeAndHolidayDate(countryCode, holidayDate)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.updateHoliday(countryCode, holidayName, holidayDate));
    }


    @Test
    void deleteHolidayByCountryCodeAndHolidayDate_validInput_deletesHoliday() {
        String countryCode = "USA";
        LocalDate holidayDate = LocalDate.now();
        Country country = new Country();
        country.setCountryCode(countryCode);

        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(countryRepository.findByCountryCode(countryCode)).thenReturn(country);
        when(federalHolidayRepository.deleteByCountryAndHolidayDate(country, holidayDate)).thenReturn(1);

        federalHolidayService.deleteHolidayByCountryCodeAndHolidayDate(countryCode, holidayDate);

        verify(federalHolidayRepository).deleteByCountryAndHolidayDate(country, holidayDate);
    }

    @Test
    void deleteHolidayByCountryCodeAndHolidayDate_invalidCountryCode_throwsInvalidCountryCodeException() {
        String countryCode = "XYZ";
        LocalDate holidayDate = LocalDate.now();

        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(false);

        assertThrows(InvalidCountryCodeException.class, () -> federalHolidayService.deleteHolidayByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }

    @Test
    void deleteHolidayByCountryCodeAndHolidayDate_holidayNotFound_throwsIllegalArgumentException() {
        String countryCode = "USA";
        LocalDate holidayDate = LocalDate.now();
        Country country = new Country();
        country.setCountryCode(countryCode);

        when(countryRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(countryRepository.findByCountryCode(countryCode)).thenReturn(country);
        when(federalHolidayRepository.deleteByCountryAndHolidayDate(country, holidayDate)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.deleteHolidayByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }


    @Test
    void processMultipleCsvFiles_processesFilesCorrectly() throws IOException {
        MultipartFile file1 = new MockMultipartFile("file1.csv", "data1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2.csv", "data2".getBytes());
        List<MultipartFile> files = List.of(file1, file2);


        ResponseEntity<Map<String, Object>> responseEntity = federalHolidayService.processMultipleCsvFiles(files);

        assertEquals(200, responseEntity.getStatusCodeValue());
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(2, ((List<?>) response.get("files_processed")).size());
    }

    @Test
    void processMultipleCsvFiles_handlesEmptyFile() {
        MultipartFile file = new MockMultipartFile("empty.csv", "empty.csv", "text/csv", new byte[0]);
        List<MultipartFile> files = List.of(file);

        ResponseEntity<Map<String, Object>> responseEntity = federalHolidayService.processMultipleCsvFiles(files);

        assertEquals(200, responseEntity.getStatusCodeValue());
        Map<String, Object> response = responseEntity.getBody();
        assertNotNull(response);
        List<Map<String, Object>> fileResults = (List<Map<String, Object>>) response.get("files_processed");
        assertEquals(1, fileResults.size());
        assertEquals("CSV file is empty.", fileResults.get(0).get("result"));
    }

    @Test
    void processSingleCsvFile_validFile_addsHolidays() throws IOException {
        String csvData = "country_code,holiday_name,holiday_date\n" +
                "USA,Test Holiday,01-01-2024";
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());
        Country country = new Country();
        country.setCountryCode("USA");

        when(countryRepository.findByCountryCode("USA")).thenReturn(country);
        when(dateUtilService.calculateDayOfWeek(any(LocalDate.class))).thenReturn(DayOfWeekEnum.MONDAY);
        when(federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(any(Country.class), anyString(), any(LocalDate.class))).thenReturn(false);

        Map<String, Object> result = federalHolidayService.processSingleCsvFile(file);

        assertEquals(1, result.get("added_records_count"));
        assertTrue(((List<?>) result.get("duplicate_rows")).isEmpty());
        assertTrue(((List<?>) result.get("invalid_rows")).isEmpty());
    }

    @Test
    void processSingleCsvFile_duplicateHoliday_handlesDuplicate() throws IOException {
        String csvData = "country_code,holiday_name,holiday_date\n" +
                "USA,Test Holiday,01-01-2024";
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());
        Country country = new Country();
        country.setCountryCode("USA");

        when(countryRepository.findByCountryCode("USA")).thenReturn(country);
        when(dateUtilService.calculateDayOfWeek(any(LocalDate.class))).thenReturn(DayOfWeekEnum.MONDAY);
        when(federalHolidayRepository.existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(any(Country.class), anyString(), any(LocalDate.class))).thenReturn(true);

        Map<String, Object> result = federalHolidayService.processSingleCsvFile(file);

        assertEquals(0, result.get("added_records_count"));
        assertEquals(1, ((List<?>) result.get("duplicate_rows")).size());
        assertTrue(((List<?>) result.get("invalid_rows")).isEmpty());
    }

    @Test
    void processSingleCsvFile_invalidDateFormat_handlesInvalidDate() throws IOException {
        String csvData = "country_code,holiday_name,holiday_date\n" +
                "USA,Test Holiday,01/01/2024"; // Invalid date format
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());
        Country country = new Country();
        country.setCountryCode("USA");

        when(countryRepository.findByCountryCode("USA")).thenReturn(country);

        Map<String, Object> result = federalHolidayService.processSingleCsvFile(file);

        assertEquals(0, result.get("added_records_count"));
        assertTrue(((List<?>) result.get("duplicate_rows")).isEmpty());
        assertEquals(1, ((List<?>) result.get("invalid_rows")).size());
        assertTrue(((String) ((List<?>) result.get("invalid_rows")).get(0)).contains("Invalid date format"));
    }
    @Test
    void processSingleCsvFile_missingColumn_throwsException() throws IOException{
        String csvData = "country_code,holiday_name\n" +  // Missing holiday_date
                "USA,Test Holiday";
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.processSingleCsvFile(file));
    }
    @Test
    void processSingleCsvFile_invalidCountryCodeInRow_handlesInvalidCountryCodeInRow() throws IOException {
        String csvData = "country_code,holiday_name,holiday_date\n" +
                "XX,Test Holiday,01-01-2024"; // Invalid country code in row
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());

        Map<String, Object> result = federalHolidayService.processSingleCsvFile(file);

        assertEquals(0, result.get("added_records_count"));
        assertTrue(((List<?>) result.get("duplicate_rows")).isEmpty());
        assertEquals(1, ((List<?>) result.get("invalid_rows")).size());
        assertTrue(((String) ((List<?>) result.get("invalid_rows")).get(0)).contains("Invalid Country code"));


    }
    @Test
    void processSingleCsvFile_countryNotFoundInRow_handlesCountryNotFound() throws IOException {
        String csvData = "country_code,holiday_name,holiday_date\n" +
                "USA,Test Holiday,01-01-2024";
        MultipartFile file = new MockMultipartFile("data.csv", csvData.getBytes());

        when(countryRepository.findByCountryCode("USA")).thenReturn(null); // Country not found

        Map<String, Object> result = federalHolidayService.processSingleCsvFile(file);

        assertEquals(0, result.get("added_records_count"));
        assertTrue(((List<?>) result.get("duplicate_rows")).isEmpty());
        assertEquals(1, ((List<?>) result.get("invalid_rows")).size());
        assertTrue(((String) ((List<?>) result.get("invalid_rows")).get(0)).contains("Country not found"));
    }
}