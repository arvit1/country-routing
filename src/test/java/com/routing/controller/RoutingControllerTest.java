package com.routing.controller;

import com.routing.exception.NoRouteException;
import com.routing.model.RouteResponse;
import com.routing.service.CountryGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingControllerTest {

    @Mock
    private CountryGraphService countryGraphService;

    private RoutingController routingController;

    @BeforeEach
    void setUp() {
        routingController = new RoutingController(countryGraphService);
    }

    @Nested
    class GetRoute {

        @Test
        void shouldReturnRouteWhenServiceReturnsRoute() {
            // given
            var route = List.of("CZE", "AUT", "ITA");
            when(countryGraphService.findRoute("CZE", "ITA")).thenReturn(route);

            // when
            ResponseEntity<RouteResponse> response = routingController.getRoute("CZE", "ITA");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().route()).isEqualTo(route);
            verify(countryGraphService).findRoute("CZE", "ITA");
        }

        @Test
        void shouldThrowNoRouteExceptionWhenServiceThrowsNoRouteException() {
            // given
            var exception = new NoRouteException("No land route found from 'JPN' to 'KOR'");
            when(countryGraphService.findRoute(anyString(), anyString())).thenThrow(exception);

            // when & then
            assertThatThrownBy(() -> routingController.getRoute("JPN", "KOR"))
                    .isInstanceOf(NoRouteException.class)
                    .hasMessage("No land route found from 'JPN' to 'KOR'");
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void handleNoRouteExceptionShouldReturnBadRequestWithErrorMessage() {
            // given
            var exception = new NoRouteException("Unknown country code: 'XXX'");

            // when
            ResponseEntity<RoutingController.ErrorResponse> response =
                    routingController.handleNoRoute(exception);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().error()).isEqualTo("Unknown country code: 'XXX'");
        }
    }
}