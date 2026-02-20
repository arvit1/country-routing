package com.routing.model;

import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountryTest {

    @Nested
    class ConstructorValidation {

        @Test
        void shouldThrowExceptionWhenCca3IsNull() {
            assertThatThrownBy(() -> new Country(null, List.of("AUT")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("cca3 should not be null");
        }

        @Test
        void shouldAcceptNullBordersAndConvertToEmptyList() {
            var country = new Country("CZE", null);
            assertThat(country.borders()).isEmpty();
        }

        @Test
        void shouldCreateImmutableCopyOfBorders() {
            var mutableBorders = new java.util.ArrayList<String>();
            mutableBorders.add("AUT");
            mutableBorders.add("SVK");

            var country = new Country("CZE", mutableBorders);
            // Original list can be modified without affecting the record
            mutableBorders.add("POL");
            assertThat(country.borders()).containsExactly("AUT", "SVK");
            assertThat(country.borders()).hasSize(2);
        }

        @Test
        void bordersListShouldBeImmutable() {
            var country = new Country("CZE", List.of("AUT", "SVK"));
            List<String> borders = country.borders();
            assertThatThrownBy(() -> borders.add("POL"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class RecordProperties {

        @Test
        void shouldReturnCca3() {
            var country = new Country("CZE", List.of("AUT", "SVK"));
            assertThat(country.cca3()).isEqualTo("CZE");
        }

        @Test
        void shouldReturnBorders() {
            var borders = List.of("AUT", "SVK");
            var country = new Country("CZE", borders);
            assertThat(country.borders()).isEqualTo(borders);
        }
    }
}