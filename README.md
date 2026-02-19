# Country Routing Service

A Spring Boot service that calculates the shortest land route between any two countries using BFS over the public country borders dataset.

## Java 25 Features Used

| Feature | Where |
|---|---|
| **Records** | `Country`, `RouteResponse`, `BfsState`, `ErrorResponse` — immutable data carriers |
| **Virtual Threads** | HTTP client executor, Spring MVC request dispatcher, startup graph loader |
| **Functional programming** | `Function`, `Predicate` composition in BFS; stream pipelines for graph building and JSON parsing |
| **Pattern matching / text blocks** | `formatted()` strings, `Optional` chains |

---

## Prerequisites

- **Java 25** (preview features enabled)
- **Maven 3.9+**

Verify:
```bash
java -version   # should show 25
mvn -version
```

---

## Build

```bash
cd country-routing
mvn clean package -DskipTests
```

---

## Run

```bash
java -jar target/country-routing-1.0.0.jar

```

The app fetches country data from GitHub on startup and logs:
```
Loaded border graph with 250 countries
```

---

## API

### `GET /routing/{origin}/{destination}`

Returns the shortest land route between two countries identified by their `cca3` codes.

**Success — 200 OK**
```bash
curl http://localhost:8080/routing/CZE/ITA
```
```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

**No land route — 400 Bad Request**
```bash
curl http://localhost:8080/routing/JPN/KOR
```
```json
{
  "error": "No land route found from 'JPN' to 'KOR'"
}
```

**Unknown country code — 400 Bad Request**
```bash
curl http://localhost:8080/routing/XXX/DEU
```
```json
{
  "error": "Unknown country code: 'XXX'"
}
```

---

## Test

```bash
mvn test
```

Tests cover: CZE→ITA (exact path), same-country shortcut, island country (JPN), unknown code, and multi-hop routes.

---

## Architecture & Design Decisions

### Why BFS?
The border graph is unweighted. BFS guarantees the **minimum number of border crossings** in O(V + E) — optimal for ~250 nodes.

### Why Virtual Threads?
- The HTTP fetch to GitHub is blocking I/O — virtual threads handle this efficiently without reactive boilerplate
- Spring Boot 3.x dispatches each HTTP request on a virtual thread when `spring.threads.virtual.enabled=true`
- No thread pool sizing needed; the JVM manages carrier thread scheduling transparently

### Graph snapshot per request
`findRoute` captures `borderGraph` into a local variable. This ensures a consistent snapshot even if the graph were ever hot-reloaded.

---

## Configuration

Override the data URL via property or env var:

```bash
java -Dcountries.data.url=http://my-mirror/countries.json \
  -jar target/country-routing-1.0.0.jar
```
