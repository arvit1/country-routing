package com.routing.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteResponseTest {

    @Nested
    class ConstructorValidation {

        @Test
        void shouldThrowExceptionWhenRouteIsNull() {
            assertThatThrownBy(() -> new RouteResponse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldCreateImmutableCopyOfRoute() {
            var mutableRoute = new java.util.ArrayList<String>();
            mutableRoute.add("CZE");
            mutableRoute.add("AUT");

            var response = new RouteResponse(mutableRoute);
            // Modify original list
            mutableRoute.add("ITA");
            assertThat(response.route()).containsExactly("CZE", "AUT");
            assertThat(response.route()).hasSize(2);
        }

        @Test
        void routeListShouldBeImmutable() {
            var response = new RouteResponse(List.of("CZE", "AUT"));
            List<String> route = response.route();
            assertThatThrownBy(() -> route.add("ITA"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class RecordProperties {

        @Test
        void shouldReturnRoute() {
            var route = List.of("CZE", "AUT", "ITA");
            var response = new RouteResponse(route);
            assertThat(response.route()).isEqualTo(route);
        }
    }
}