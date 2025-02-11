package com.holiday.repository;

import com.holiday.entity.FederalHoliday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FederalHolidayRepository extends JpaRepository<FederalHoliday,Long> {
    Page<FederalHoliday> findByCountry_CountryCode(String countryCode, Pageable pageable);

    @Modifying
    @Query("DELETE FROM FederalHoliday f WHERE f.country.countryCode = :countryCode")
    void deleteByCountryCode(@Param("countryCode") String countryCode);

}
