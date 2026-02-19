package com.routing.controller;

import com.routing.exception.NoRouteException;
import com.routing.model.RouteResponse;
import com.routing.service.CountryGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the /routing/{origin}/{destination} endpoint.
 *
 * Spring Boot 3.x with virtual threads enabled (see application.properties)
 * means each request is handled on a virtual thread automatically — no need
 * for reactive/WebFlux complexity.
 */
@RestController
@RequestMapping("/routing")
public class RoutingController {

    private final CountryGraphService countryGraphService;

    public RoutingController(CountryGraphService countryGraphService) {
        this.countryGraphService = countryGraphService;
    }

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable String origin,
            @PathVariable String destination
    ) {
        var route = countryGraphService.findRoute(
                origin.toUpperCase(),
                destination.toUpperCase()
        );
        return ResponseEntity.ok(new RouteResponse(route));
    }

    @ExceptionHandler(NoRouteException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoRouteException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    record ErrorResponse(String error) {}
}
