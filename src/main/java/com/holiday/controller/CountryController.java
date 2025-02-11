package com.holiday.controller;

import com.holiday.entity.Country;
import com.holiday.service.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;


    @GetMapping
    public ResponseEntity<Page<Country>> getAllCountries(Pageable pageable) {
        return ResponseEntity.ok(countryService.getAllCountries(pageable));
    }

    @GetMapping("/{countryCode}")
    public ResponseEntity<Country> getCountryByCode(@PathVariable String countryCode) {
        return ResponseEntity.ok(countryService.getCountryByCode(countryCode));
    }

    @PostMapping
    public ResponseEntity<Country> addCountry(@RequestBody Country country) {
        return ResponseEntity.ok(countryService.addCountry(country));
    }

    @PutMapping("/{countryCode}")
    public ResponseEntity<Country> updateCountry(
            @PathVariable String countryCode,
            @RequestBody Country updatedCountry) {
        return ResponseEntity.ok(countryService.updateCountry(countryCode, updatedCountry));
    }

    @DeleteMapping("/{countryCode}")
    public ResponseEntity<Void> deleteCountry(@PathVariable String countryCode) {
        countryService.deleteCountry(countryCode);
        return ResponseEntity.noContent().build();
    }
}
