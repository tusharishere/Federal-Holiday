package com.holiday.service.service;

import com.holiday.entity.Country;
import com.holiday.repository.CountryRepository;
import com.holiday.service.Impl.CountryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CountryService implements CountryServiceImpl {

    private final CountryRepository countryRepository;


    public Page<Country> getAllCountries(Pageable pageable) {
        return countryRepository.findAll(pageable);
    }

    public Country getCountryByCode(String countryCode) {
        return countryRepository.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));
    }

    @Transactional
    public Country addCountry(Country country) {
        if (countryRepository.existsByCountryCode(country.getCountryCode())) {
            throw new IllegalArgumentException("Country code already exists");
        }
        return countryRepository.save(country);
    }

    @Transactional
    public Country updateCountry(String countryCode, Country updatedCountry) {
        Country existingCountry = countryRepository.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));

        existingCountry.setCountryName(updatedCountry.getCountryName());
        return countryRepository.save(existingCountry);
    }

    @Transactional
    public void deleteCountry(String countryCode) {
        if (!countryRepository.existsByCountryCode(countryCode)) {
            throw new IllegalArgumentException("Country not found");
        }
        countryRepository.deleteById(countryCode);
    }
}
