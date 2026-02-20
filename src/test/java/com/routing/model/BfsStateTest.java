package com.routing.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BfsStateTest {

    @Nested
    class ConstructorValidation {

        @Test
        void shouldThrowExceptionWhenPathIsNull() {
            assertThatThrownBy(() -> new BfsState("CZE", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldAcceptNullCountry() {
            assertThatThrownBy(() -> new BfsState(null, List.of()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("country should not be null");
        }

        @Test
        void shouldCreateImmutableCopyOfPath() {
            var mutablePath = new java.util.ArrayList<String>();
            mutablePath.add("CZE");
            mutablePath.add("AUT");

            var state = new BfsState("ITA", mutablePath);
            // Modify original list
            mutablePath.add("GER");
            assertThat(state.path()).containsExactly("CZE", "AUT");
            assertThat(state.path()).hasSize(2);
        }

        @Test
        void pathListShouldBeImmutable() {
            var state = new BfsState("CZE", List.of("AUT", "GER"));
            List<String> path = state.path();
            assertThatThrownBy(() -> path.add("ITA"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class StepMethod {

        @Test
        void shouldCreateNewStateWithNeighbourAsCountry() {
            var initialState = new BfsState("CZE", List.of("CZE"));
            var nextState = initialState.step("AUT");
            assertThat(nextState.country()).isEqualTo("AUT");
        }

        @Test
        void shouldAppendNeighbourToPath() {
            var initialState = new BfsState("CZE", List.of("CZE"));
            var nextState = initialState.step("AUT");
            assertThat(nextState.path()).containsExactly("CZE", "AUT");
        }

        @Test
        void shouldNotModifyOriginalState() {
            var initialState = new BfsState("CZE", List.of("CZE"));
            var nextState = initialState.step("AUT");
            // Original state unchanged
            assertThat(initialState.country()).isEqualTo("CZE");
            assertThat(initialState.path()).containsExactly("CZE");
        }

        @Test
        void shouldHandleEmptyPath() {
            var initialState = new BfsState("CZE", List.of());
            var nextState = initialState.step("AUT");
            assertThat(nextState.path()).containsExactly("AUT");
        }

        @Test
        void shouldHandleMultipleSteps() {
            var state1 = new BfsState("CZE", List.of("CZE"));
            var state2 = state1.step("AUT");
            var state3 = state2.step("GER");
            assertThat(state3.country()).isEqualTo("GER");
            assertThat(state3.path()).containsExactly("CZE", "AUT", "GER");
        }
    }

    @Nested
    class RecordProperties {

        @Test
        void shouldReturnCountry() {
            var state = new BfsState("CZE", List.of("CZE", "AUT"));
            assertThat(state.country()).isEqualTo("CZE");
        }

        @Test
        void shouldReturnPath() {
            var path = List.of("CZE", "AUT");
            var state = new BfsState("CZE", path);
            assertThat(state.path()).isEqualTo(path);
        }
    }
}