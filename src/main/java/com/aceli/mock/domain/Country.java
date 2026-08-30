package com.aceli.mock.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Country {
    KENYA("KE", "Kenya"),
    UGANDA("UG", "Uganda"),
    TANZANIA("TZ", "Tanzania"),
    RWANDA("RW", "Rwanda"),
    ZAMBIA("ZM", "Zambia");

    private final String code;
    private final String displayName;

    Country(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    @JsonValue
    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static Country from(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(country -> country.name().equalsIgnoreCase(value)
                        || country.code.equalsIgnoreCase(value)
                        || country.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported country: " + value));
    }
}
