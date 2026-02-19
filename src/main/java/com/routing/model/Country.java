package com.routing.model;

import java.util.List;

/**
 * Immutable record representing a country from the external JSON data.
 */
public record Country(
        String cca3,
        List<String> borders
) {
    public Country {
        borders = borders == null ? List.of() : List.copyOf(borders);
    }
}
