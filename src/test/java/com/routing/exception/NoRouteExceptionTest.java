package com.routing.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoRouteExceptionTest {

    @Test
    void shouldStoreAndReturnMessage() {
        var message = "No land route found from 'JPN' to 'KOR'";
        var exception = new NoRouteException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldBeRuntimeException() {
        var exception = new NoRouteException("test");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldAllowNullMessage() {
        var exception = new NoRouteException(null);
        assertThat(exception.getMessage()).isNull();
    }
}