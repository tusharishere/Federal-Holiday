package com.holiday.repository;

import com.holiday.entity.Country;
import com.holiday.entity.FederalHoliday;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FederalHolidayRepository extends JpaRepository<FederalHoliday,Long> {

    List<FederalHoliday> findByCountry_CountryCode(String countryCode);
    Optional<FederalHoliday> findByCountry_CountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
    boolean existsByCountry_CountryCode(String countryCode);

    @Modifying
    @Query("DELETE FROM FederalHoliday f WHERE f.country.countryCode = :countryCode")
    void deleteByCountryCode(@Param("countryCode") String countryCode);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FederalHoliday f " +
            "WHERE f.country = :country AND LOWER(f.holidayName) = LOWER(:holidayName) AND f.holidayDate = :holidayDate")
    boolean existsByCountryAndHolidayNameAndHolidayDateIgnoreCase(
            @Param("country") Country country,
            @Param("holidayName") String holidayName,
            @Param("holidayDate") LocalDate holidayDate);


    @Transactional
    int deleteByCountryAndHolidayDate(Country country, LocalDate holidayDate);
}
