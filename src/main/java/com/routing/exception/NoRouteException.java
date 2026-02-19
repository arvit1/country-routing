package com.routing.exception;

/**
 * Thrown when no land route exists between two countries,
 * or when an unknown country code is provided.
 */
public class NoRouteException extends RuntimeException {
    public NoRouteException(String message) {
        super(message);
    }
}
