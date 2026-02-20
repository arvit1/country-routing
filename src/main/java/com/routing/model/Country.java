package com.routing.model;

import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Immutable record representing a country from the external JSON data.
 */
public record Country(
        String cca3,
        List<String> borders
) {
    public Country {
        Validate.notNull(cca3, "cca3 should not be null");
        borders = borders == null ? List.of() : List.copyOf(borders);
    }
}
