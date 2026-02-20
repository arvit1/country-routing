package com.routing.model;

import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Immutable record representing the API response for a route query.
 */
public record RouteResponse(List<String> route) {
    public RouteResponse {
        Validate.notNull(route, "route should not be null");
        route = List.copyOf(route);
    }
}
