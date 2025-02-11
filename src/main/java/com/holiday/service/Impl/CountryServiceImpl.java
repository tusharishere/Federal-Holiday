package com.holiday.service.Impl;

import com.holiday.entity.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryServiceImpl {
    Page<Country> getAllCountries(Pageable pageable);
    Country getCountryByCode(String countryCode);
    Country addCountry(Country country);
    Country updateCountry(String countryCode, Country updatedCountry);
    void deleteCountry(String countryCode);
}
