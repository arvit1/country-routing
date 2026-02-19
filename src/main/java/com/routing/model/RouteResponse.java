package com.routing.model;

import java.util.List;

/**
 * Immutable record representing the API response for a route query.
 */
public record RouteResponse(List<String> route) {
    public RouteResponse {
        route = List.copyOf(route);
    }
}
