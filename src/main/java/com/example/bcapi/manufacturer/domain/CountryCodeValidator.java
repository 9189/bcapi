package com.example.bcapi.manufacturer.domain;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class CountryCodeValidator {

    private static final Set<String> VALID_CODES = Set.of(Locale.getISOCountries());

    public void validate(String countryCode) {
        if (countryCode == null || countryCode.length() != 2 || !countryCode.equals(countryCode.toUpperCase()) || !VALID_CODES.contains(countryCode)) {
            throw new InvalidCountryCodeException(countryCode);
        }
    }
}
