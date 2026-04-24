# Smart Campus REST API (JAX-RS Jersey)

## API Overview

This project implements a Smart Campus REST API using Java, JAX-RS (Jersey), Maven, and in-memory collections (`HashMap`/`ArrayList`) only.  
Base path: `/api/v1`

Main resources:

- `rooms`
- `sensors`
- `sensors/{sensorId}/readings` (sub-resource)

## Build And Run

1. Open terminal in project root.
2. Build WAR:
   - `mvn clean package`
3. Deploy `target/smart-campus-api.war` to Apache Tomcat 9.
4. Start Tomcat.
5. Access:
   - `http://localhost:8080/smart-campus-api/api/v1`

## Curl Testing (at least 5)

1. Discovery
   - `curl -X GET http://localhost:8080/smart-campus-api/api/v1`
2. Create room
   - `curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":80}"`
3. List rooms
   - `curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms`
4. Create sensor (valid roomId)
   - `curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":410.0,\"roomId\":\"LIB-301\"}"`
5. Filter sensors by type
   - `curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"`
6. Add sensor reading
   - `curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings -H "Content-Type: application/json" -d "{\"value\":435.5}"`
7. Get sensor readings
   - `curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings`
8. Try deleting room that still has sensors (409 expected)
   - `curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301`
9. Create sensor with invalid roomId (422 expected)
   - `curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-404\",\"type\":\"TEMPERATURE\",\"status\":\"ACTIVE\",\"currentValue\":25.0,\"roomId\":\"NO-ROOM\"}"`

## Coursework Question Answers

### Part 1

1. **JAX-RS Resource Lifecycle**

- By default, JAX-RS creates a new resource instance per request.
- This avoids shared mutable state inside resource objects.
- Because data is kept in static in-memory collections, concurrent requests can still touch shared maps/lists. To reduce race-condition risk, write logic is centralised in predictable service paths while resource classes remain stateless. In production hardening, critical write sections should be protected with synchronisation/locks to avoid lost updates under concurrent traffic.

2. **Why Hypermedia/HATEOAS matters**

- Hypermedia links tell clients where to go next directly in responses.
- Clients can discover API navigation from the server response instead of hardcoding every path from static documentation.
- This reduces coupling and improves maintainability when endpoints evolve.

### Part 2

3. **Returning IDs only vs full room objects**

- IDs-only reduces payload size and bandwidth.
- Full objects reduce extra client requests and simplify client processing.
- For this coursework, returning full room objects in list endpoints keeps client usage straightforward.

4. **Is DELETE idempotent**

- Yes. If the same valid delete request is repeated after successful deletion, server state does not change further.
- In this implementation, first delete removes the room; repeated delete returns not found for that room, while the final state remains unchanged.

### Part 3

5. **Effect of wrong Content-Type with @Consumes(JSON)**

- If client sends a media type not supported by the endpoint (for example `text/plain` or `application/xml`), JAX-RS rejects it.
- The runtime returns `415 Unsupported Media Type`.

6. **Why query param is better for filtering collections**

- `GET /sensors?type=CO2` expresses optional filtering on a collection.
- Path-based designs like `/sensors/type/CO2` look like fixed nested resources, not flexible search criteria.
- Query parameters scale better for combining filters (`type`, `status`, etc.).

### Part 4

7. **Benefits of Sub-Resource Locator**

- The main `SensorResource` stays focused on sensor collection operations.
- Reading history logic is delegated to `SensorReadingResource`, making code easier to maintain.
- This separation prevents one large controller class and keeps nested endpoints clear.

8. **Reading POST side effect**

- A successful reading POST appends to sensor history and updates parent sensor `currentValue`.
- This keeps summary and historical data consistent.

### Part 5

9. **Why 422 is better than 404 for missing linked roomId in payload**

- The target endpoint exists, so this is not a missing URL resource.
- The request body is syntactically valid but semantically invalid (references a missing linked entity).
- `422 Unprocessable Entity` matches that semantic validation failure.

10. **Risks of exposing stack traces**

- Stack traces reveal internal package/class names, file paths, and framework internals.
- Attackers can use this to map the system and craft targeted attacks.
- A generic 500 response protects internals while detailed errors remain server-side logs only.

11. **Why filters for logging cross-cutting concerns**

- Filters run for all requests/responses in one place.
- This avoids duplicate logger code in every endpoint method.
- It keeps resource classes focused on business logic.
