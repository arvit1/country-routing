package com.routing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routing.exception.NoRouteException;
import com.routing.model.BfsState;
import com.routing.model.Country;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service that:
 *  - Loads country data from a remote JSON source using a virtual-thread HttpClient
 *  - Builds an adjacency graph of land borders
 *  - Exposes BFS-based route finding
 *
 * Java 25 features used:
 *  - Records (Country, BfsState, RouteResponse)
 *  - Virtual threads  (HttpClient backed by Executors.newVirtualThreadPerTaskExecutor)
 *  - Functional style (streams, Function, Predicate composition)
 *  - Pattern matching / sealed types ready (open for extension)
 */
@Service
public class CountryGraphService {

    private static final Logger log = LoggerFactory.getLogger(CountryGraphService.class);

    @Value("${countries.data.url:https://raw.githubusercontent.com/mledoze/countries/master/countries.json}")
    private String dataUrl;

    // The adjacency map: cca3 -> list of cca3 neighbours
    private volatile Map<String, List<String>> borderGraph = Map.of();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CountryGraphService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
// -------------------------------------------------------------------------
    // Startup: load & parse data
    // -------------------------------------------------------------------------

    @PostConstruct
    void init() throws Exception {
        try {
            borderGraph = buildGraph(fetchCountries());
            log.info("Loaded border graph with {} countries", borderGraph.size());
        } catch (Exception e) {
            log.error("Failed to load country data", e);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Find the shortest land route from {@code origin} to {@code destination}.
     * runs BFS on a virtual thread.
     *
     * @return ordered list of cca3 codes from origin to destination (inclusive)
     * @throws NoRouteException if no land route exists or codes are unknown
     */
    public List<String> findRoute(String origin, String destination) {
        var graph = borderGraph;

        validateCountry(graph, origin);
        validateCountry(graph, destination);

        if (origin.equals(destination)) {
            return List.of(origin);
        }

        return bfs(graph, origin, destination);

    }

    // -------------------------------------------------------------------------
    // BFS — functional style
    // -------------------------------------------------------------------------

    /**
     * Breadth-first search over the border graph.
     */
    private List<String> bfs(Map<String, List<String>> graph, String origin, String destination) {
        var visited = new HashSet<String>();
        visited.add(origin);

        // Functional predicate: has this country been visited?
        Predicate<String> notVisited = Predicate.not(visited::contains);

        // Functional mapper: given a BfsState, produce all valid next states
        Function<BfsState, List<BfsState>> expand = state ->
                graph.getOrDefault(state.country(), List.of())
                        .stream()
                        .filter(notVisited)
                        .peek(visited::add)
                        .map(state::step)
                        .collect(Collectors.toList());

        // BFS queue seeded with the initial state
        var queue = new ArrayDeque<BfsState>();
        queue.add(new BfsState(origin, new ArrayList<>(List.of(origin))));

        while (!queue.isEmpty()) {
            var current = queue.poll();

            if (current.country().equals(destination)) {
                return current.path();
            }

            queue.addAll(expand.apply(current));
        }

        throw new NoRouteException(
                "No land route found from '%s' to '%s'".formatted(origin, destination));
    }

    // -------------------------------------------------------------------------
    // Graph construction — pure functional pipeline
    // -------------------------------------------------------------------------

    private Map<String, List<String>> buildGraph(List<Country> countries) {
        return countries.stream()
                .filter(c -> c.cca3() != null && !c.cca3().isBlank())
                .collect(Collectors.toUnmodifiableMap(
                        Country::cca3,
                        Country::borders,
                        (a, b) -> a   // merge function: keep first on duplicate key
                ));
    }

    // -------------------------------------------------------------------------
    // HTTP fetch — virtual-thread HttpClient
    // -------------------------------------------------------------------------

    private List<Country> fetchCountries() throws Exception {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(dataUrl))
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Unexpected HTTP status: " + response.statusCode());
            }

            // Parse into raw maps first (JSON has nested objects we don't need)
            List<Map<String, Object>> raw = objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

            // Functional pipeline: raw map → Country record
            return raw.stream()
                    .map(this::toCountry)
                    .collect(Collectors.toList());
    }

    private Country toCountry(Map<String, Object> raw) {
        String cca3 = (String) raw.get("cca3");

        var borders = Optional.ofNullable(raw.get("borders"))
                .filter(b -> b instanceof List<?>)
                .map(b -> (List<String>) b)
                .orElse(List.of());

        return new Country(cca3, borders);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void validateCountry(Map<String, List<String>> graph, String code) {
        if (!graph.containsKey(code)) {
            throw new NoRouteException("Unknown country code: '%s'".formatted(code));
        }
    }
}
