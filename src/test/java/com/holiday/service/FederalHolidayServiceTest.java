package com.holiday.service;
import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import com.holiday.enums.DayOfWeekEnum;
import com.holiday.exception.DuplicateHolidayException;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.FederalHolidayRepository;
import com.holiday.service.service.CountryService;
import com.holiday.service.service.FederalHolidayService;
import com.holiday.utils.DateUtilService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FederalHolidayServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Mock
    private FederalHolidayRepository federalHolidayRepository;

    @InjectMocks
    private FederalHolidayService federalHolidayService;

    @Mock
    private DateUtilService dateUtilService;

    private Country testCountry;
    private FederalHoliday testHoliday;

    @BeforeEach
    void setUp() {
        testCountry = new Country();
        testCountry.setCountryCode("TST");
        testCountry.setCountryName("Test Country");

        testHoliday = new FederalHoliday();
        testHoliday.setHolidayName("Test Holiday");
        testHoliday.setHolidayDate(LocalDate.of(2024, 1, 1));
        testHoliday.setCountry(testCountry);
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void getAllCountries_shouldReturnPageOfCountries() {
        Pageable pageable = Pageable.unpaged();
        Page<Country> countries = new PageImpl<>(Collections.singletonList(testCountry));
        when(countryRepository.findAll(pageable)).thenReturn(countries);

        Page<Country> result = countryService.getAllCountries(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testCountry, result.getContent().get(0));
    }

    @Test
    void getCountryByCode_shouldReturnCountry() {
        when(countryRepository.findById("TST")).thenReturn(Optional.of(testCountry));

        Country result = countryService.getCountryByCode("TST");

        assertEquals(testCountry, result);
    }

    @Test
    void getCountryByCode_shouldThrowExceptionIfNotFound() {
        when(countryRepository.findById("TST")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> countryService.getCountryByCode("TST"));
    }

    @Test
    void addCountry_shouldReturnAddedCountry() {
        when(countryRepository.existsByCountryCode("TST")).thenReturn(false);
        when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

        Country result = countryService.addCountry(testCountry);

        assertEquals(testCountry, result);
    }

    @Test
    void addCountry_shouldThrowExceptionIfCodeExists() {
        when(countryRepository.existsByCountryCode("TST")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> countryService.addCountry(testCountry));
    }

    @Test
    void updateCountry_shouldReturnUpdatedCountry() {
        Country updatedCountry = new Country();
        updatedCountry.setCountryName("Updated Test Country");
        when(countryRepository.findById("TST")).thenReturn(Optional.of(testCountry));
        when(countryRepository.save(any(Country.class))).thenReturn(updatedCountry);

        Country result = countryService.updateCountry("TST", updatedCountry);

        assertEquals(updatedCountry.getCountryName(), result.getCountryName());
    }

    @Test
    void updateCountry_shouldThrowExceptionIfNotFound() {
        when(countryRepository.findById("TST")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> countryService.updateCountry("TST", new Country()));
    }

    @Test
    void deleteCountry_shouldDeleteCountry() {
        when(countryRepository.existsByCountryCode("TST")).thenReturn(true);

        countryService.deleteCountry("TST");

        verify(countryRepository).deleteById("TST");
    }

    @Test
    void deleteCountry_shouldThrowExceptionIfNotFound() {
        when(countryRepository.existsByCountryCode("TST")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> countryService.deleteCountry("TST"));
    }


    @Test
    void getHolidaysByCountry_shouldReturnHolidays() {
        Pageable pageable = Pageable.unpaged();
        Page<FederalHoliday> holidays = new PageImpl<>(Collections.singletonList(testHoliday));
        when(federalHolidayRepository.findByCountry_CountryCode("TST", pageable)).thenReturn(holidays);

        Page<FederalHoliday> result = federalHolidayService.getHolidaysByCountry("TST", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testHoliday, result.getContent().get(0));
    }

    @Test
    void deleteHolidaysByCountry_shouldDeleteHolidays() {
        federalHolidayService.deleteHolidaysByCountry("TST");
        verify(federalHolidayRepository).deleteByCountryCode("TST");
    }

    @Test
    void getHolidays_shouldReturnAllHolidays() {
        List<FederalHoliday> holidays = Collections.singletonList(testHoliday);
        when(federalHolidayRepository.findAll()).thenReturn(holidays);

        List<FederalHoliday> result = federalHolidayService.getHolidays();

        assertEquals(1, result.size());
        assertEquals(testHoliday, result.get(0));
    }

    @Test
    void getHolidayById_shouldReturnHoliday() {
        when(federalHolidayRepository.findById(1L)).thenReturn(Optional.of(testHoliday));

        FederalHoliday result = federalHolidayService.getHolidayById(1L);

        assertEquals(testHoliday, result);
    }

    @Test
    void getHolidayById_shouldThrowExceptionIfNotFound() {
        when(federalHolidayRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.getHolidayById(1L));
    }

    @Test
    void addHoliday_shouldReturnAddedHoliday() {
        when(countryRepository.findById("TST")).thenReturn(Optional.of(testCountry));
        when(dateUtilService.calculateDayOfWeek(any(LocalDate.class))).thenReturn(DayOfWeekEnum.valueOf("MONDAY"));
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(testHoliday);

        FederalHoliday result = federalHolidayService.addHoliday("TST", "Test Holiday", LocalDate.of(2024, 1, 1));

        assertEquals(testHoliday, result);
    }


    @Test
    void addHoliday_shouldThrowDuplicateHolidayException() {
        when(countryRepository.findById("TST")).thenReturn(Optional.of(testCountry));
        when(dateUtilService.calculateDayOfWeek(any(LocalDate.class))).thenReturn(DayOfWeekEnum.valueOf("MONDAY"));
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenThrow(new RuntimeException("Duplicate entry")); // Simulate database constraint

        assertThrows(DuplicateHolidayException.class, () -> federalHolidayService.addHoliday("TST", "Test Holiday", LocalDate.of(2024, 1, 1)));
    }


    @Test
    void addHoliday_shouldThrowExceptionIfCountryNotFound() {
        when(countryRepository.findById("TST")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> federalHolidayService.addHoliday("TST", "Test Holiday", LocalDate.of(2024, 1, 1)));
    }


    @Test
    void updateHoliday_shouldReturnUpdatedHoliday() {
        FederalHoliday updatedHoliday = new FederalHoliday();
        updatedHoliday.setHolidayName("Updated Test Holiday");
        updatedHoliday.setHolidayDate(LocalDate.of(2024, 1, 2));
        updatedHoliday.setCountry(testCountry);  // Important: Set the

    }
}