package com.routing.model;

import java.util.List;

/**
 * Immutable record capturing BFS traversal state — the current country
 * and the full path taken to reach it.
 */
public record BfsState(String country, List<String> path) {

    public BfsState {
        path = List.copyOf(path);
    }

    /** Derive a new state by stepping into a neighbouring country. */
    public BfsState step(String neighbour) {
        var newPath = new java.util.ArrayList<>(path);
        newPath.add(neighbour);
        return new BfsState(neighbour, newPath);
    }
}
