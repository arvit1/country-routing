// src/test/java/com/routing/service/CountryGraphServiceIT.java
package com.routing.service;

import com.routing.exception.NoRouteException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CountryGraphServiceTest {

    @Autowired
    CountryGraphService countryGraphService;

    @Nested
    class LandRoutes {

        @Test
        void czechToItaly_landRoute() {
            var route = countryGraphService.findRoute("CZE", "ITA");
            assertThat(route).isEqualTo(List.of("CZE", "AUT", "ITA"));
        }

        @Test
        void multiHopRoute_asia() {
            var route = countryGraphService.findRoute("KAZ", "KOR");
            assertThat(route).isEqualTo(List.of("KAZ", "CHN", "PRK", "KOR"));
        }

        @Test
        void sameCountry_returnsSingleElement() {
            var route = countryGraphService.findRoute("FRA", "FRA");
            assertThat(route).containsExactly("FRA");
        }
    }

    @Nested
    class IslandCases {

        @Test
        void japanToKorea_noLandRoute() {
            var ex = assertThrows(NoRouteException.class,
                    () -> countryGraphService.findRoute("JPN", "KOR"));
            assertThat(ex.getMessage()).contains("No land route");
        }

        @Test
        void ukToFrance_noLandRouteDespiteChannel() {
            var ex = assertThrows(NoRouteException.class,
                    () -> countryGraphService.findRoute("GBR", "FRA"));
            assertThat(ex.getMessage()).contains("No land route");
        }
    }

    @Nested
    class PeninsulaAndNarrowCases {

        @Test
        void italyToSpain_viaFrance() {
            var route = countryGraphService.findRoute("ITA", "ESP");
            assertThat(route).isEqualTo(List.of("ITA", "FRA", "ESP"));
        }

        @Test
        void portugalToGermany_crossMultipleBorders() {
            var route = countryGraphService.findRoute("PRT", "DEU");
            assertThat(route).isEqualTo(List.of("PRT", "ESP", "FRA", "DEU"));
        }
    }

    @Nested
    class EdgeConditions {

        @Test
        void unknownOrigin_throws() {
            var ex = assertThrows(NoRouteException.class,
                    () -> countryGraphService.findRoute("XXX", "DEU"));
            assertThat(ex.getMessage()).contains("Unknown country code");
        }

        @Test
        void unknownDestination_throws() {
            var ex = assertThrows(NoRouteException.class,
                    () -> countryGraphService.findRoute("CZE", "YYY"));
            assertThat(ex.getMessage()).contains("Unknown country code");
        }

        @Test
        void disconnectedTerritory_noLandRoute() {
            // Example: often island/overseas territories like "GRL" (Greenland) to "CAN"
            var ex = assertThrows(NoRouteException.class,
                    () -> countryGraphService.findRoute("GRL", "CAN"));
            assertThat(ex.getMessage()).contains("No land route");
        }
    }
}
