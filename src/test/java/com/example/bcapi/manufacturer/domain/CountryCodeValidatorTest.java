package com.example.bcapi.manufacturer.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountryCodeValidatorTest {

    private final CountryCodeValidator validator = new CountryCodeValidator();

    @ParameterizedTest
    @ValueSource(strings = {"NL", "DE", "AT", "US", "ES"})
    void validate_validCode_doesNotThrow(String countryCode) {
        assertThatNoException().isThrownBy(() -> validator.validate(countryCode));
    }

    @Test
    void validate_lowercase_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("nl"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_tooLong_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("NLD"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_tooShort_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("N"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_empty_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate(""))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_null_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_numeric_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("12"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_specialCharacters_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("N!"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void validate_unknownCode_throwsInvalidCountryCodeException() {
        assertThatThrownBy(() -> validator.validate("XX"))
                .isInstanceOf(InvalidCountryCodeException.class);
    }
}
