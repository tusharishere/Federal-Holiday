package com.holiday.repository;

import com.holiday.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country,String> {
    boolean existsByCountryCode(String countryCode);
    Country findByCountryCode(String countryCode);
}
