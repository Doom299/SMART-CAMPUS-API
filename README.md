# Smart Campus REST API (JAX-RS Jersey)

**Student**: MALIDUWA LIYANAGE HIMASARA
**Student ID**: W2120566 / 20240256
**Module**: 5COSC022W Client–Server Architectures  
**Academic Year**: 2025/26

## Scenario Overview

This coursework implements a versioned REST API for a university “Smart Campus” monitoring system. The service manages campus **Rooms** and the **Sensors** deployed inside them (e.g., CO2, temperature, occupancy). Facilities staff can create and manage rooms, register sensors linked to existing rooms, and store historical sensor readings via a nested sub-resource. The API follows RESTful principles with meaningful HTTP status codes, JSON request/response bodies, custom exception mapping for error scenarios, and request/response logging for observability.

## API Overview

This project implements a “Smart Campus” REST API using **Java + JAX-RS (Jersey)**, built with **Maven**, and using **in-memory collections only** (no database).

- **Technology constraint compliance**:
  - Uses **JAX-RS (Jersey)** (NOT Spring Boot).
  - Uses **in-memory data structures** only (NO SQL/NoSQL database).
  - Packaged as a **WAR** and deployed to **Apache Tomcat**.

**Base path**: `/api/v1`

Main resources:

- `rooms`
- `sensors`
- `sensors/{sensorId}/readings` (sub-resource)

## Build And Run (NetBeans + Tomcat)

### Prerequisites

- **JDK 8** (project is configured for Java 8 in Maven compiler plugin)
- **Apache Tomcat 9**
- **NetBeans** (used for opening/building the project)

### Option A: Run using NetBeans (recommended)

1. Open NetBeans.
2. **File → Open Project…** and select this project folder (`SMART-CAMPUS-API`).
3. Ensure Tomcat 9 is configured in NetBeans:
   - **Services → Servers → Add Server → Apache Tomcat (Tomcat 9)** and point it to your Tomcat installation.
4. Build the project:
   - Right-click the project → **Clean and Build**
   - This runs `mvn clean package` and generates the WAR file.
5. Deploy to Tomcat:
   - Either right-click project → **Run** (if NetBeans is configured to deploy to Tomcat),
   - OR manually deploy the generated WAR (see Option B).

### Option B: Build WAR + manual Tomcat deploy

1. Open a terminal in the project root and run:
   - `mvn clean package`
2. Copy `target/smart-campus-api.war` into Tomcat’s `webapps/` folder.
3. Start Tomcat.

### Access URL

After deployment, open:

- `http://localhost:8080/smart-campus-api/api/v1`

## Curl Testing (sample evidence)

These commands demonstrate the required functionality across discovery, rooms, sensors, filtering, sub-resources, and error handling.

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

### Part 1: Service Architecture & Setup

#### 1.1 JAX-RS Resource Lifecycle (Question)

In JAX-RS, the default lifecycle for a resource class is typically **per-request** (request-scoped). That means the runtime creates a new instance of a resource class for each incoming HTTP request, and then discards it after the response is produced. This is a sensible default because it avoids sharing mutable fields inside the resource class itself; each request has its own resource object.

However, “per-request resources” does **not automatically make the application thread-safe**. In this coursework implementation, the API stores application state in **static in-memory collections** (maps/lists). Those structures are shared by all requests, regardless of how many resource instances are created. Under concurrent load (for example, multiple clients creating rooms/sensors at the same time), unsafe access to shared collections can lead to race conditions such as:

- **Lost updates** (two requests overwrite each other’s changes),
- **Inconsistent reads** (one request reads while another is mid-update),
- **Data integrity problems** (e.g., a room gets deleted while a sensor is being linked).

To manage this risk in a robust production service, I would protect shared write operations by using synchronization/locks and/or thread-safe data structures (e.g., `ConcurrentHashMap` and synchronized lists). For this coursework, I kept resource classes stateless and centralized all shared state in a dedicated store to make the design easy to reason about and demonstrate.

#### 1.2 Why Hypermedia/HATEOAS matters (Question)

Hypermedia (HATEOAS: Hypermedia As The Engine Of Application State) is considered an advanced REST characteristic because the server response does not only return data, it also provides **navigation**. In other words, the API tells the client “where to go next” using links.

This benefits client developers because:

- It reduces reliance on static documentation: a client can discover available resource collections (like rooms and sensors) directly from `GET /api/v1`.
- It reduces coupling between client and server: if paths evolve over time, clients can follow server-provided links rather than hardcoding URLs everywhere.
- It supports a more self-describing API: the response becomes a guide for interacting with the service, not only a data payload.

### Part 2: Room Management

#### 2.1 Returning IDs only vs full room objects (Question)

When returning a list of rooms (for example `GET /rooms`), there are two common strategies:

**Returning only IDs**

- Pros: smaller payload, lower bandwidth, faster transfers for very large collections.
- Cons: the client typically must make additional requests (N extra `GET /rooms/{id}` calls) to obtain details like name/capacity, which increases latency and complexity.

**Returning full room objects**

- Pros: the client immediately receives all key metadata and can render UI or run logic without extra round trips.
- Cons: larger payload size, which may matter if there are thousands of rooms.

For this coursework, returning the full room objects is a practical choice because it makes the API easier to use and demonstrate, and the dataset is small and in-memory. In a larger production system, a hybrid approach is also common (e.g., summary objects in lists, full details on `GET /{id}`).

#### 2.2 Is DELETE idempotent in your implementation? (Question)

In REST, a DELETE operation is considered **idempotent** if performing the same request multiple times results in the same final server state as performing it once. In this implementation:

- The **first** successful `DELETE /rooms/{id}` removes the room from the in-memory store.
- If the client accidentally repeats the exact same delete request afterward, the room no longer exists, so the server returns a “not found” style response.

Even though the HTTP response code might differ between the first call (success) and later calls (not found), the key point is that the **server state after the first delete is already the final state**. Repeating the delete does not remove anything else or cause additional changes. Therefore, the operation is idempotent in terms of system state.

### Part 3: Sensor Operations & Linking

#### 3.1 Effect of wrong Content-Type with `@Consumes(application/json)` (Question)

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the endpoint only accepts JSON payloads. If a client attempts to send a request body with an unsupported media type (for example `text/plain` or `application/xml`), the runtime will not attempt to deserialize it into the Java object parameter. Instead, the request is rejected and the client receives:

- **`415 Unsupported Media Type`**

This is important because it enforces a clear API contract: clients must send JSON when interacting with JSON-consuming endpoints. It also prevents confusing partial parsing and avoids undefined behavior when content types do not match.

#### 3.2 Why `@QueryParam` is superior for filtering collections (Question)

Using query parameters for filtering (e.g., `GET /sensors?type=CO2`) is generally preferred for collection searching because filters are **optional** and **combinable**. Query strings naturally support “search criteria” semantics.

If we encode filter values in the path (for example `/sensors/type/CO2`), it starts to look like a fixed resource hierarchy rather than a flexible query. It also becomes awkward when more filters are introduced (type + status + roomId), because you end up designing many path variants.

With query parameters, the API remains clean and scalable:

- `GET /sensors?type=CO2`
- `GET /sensors?type=CO2&status=ACTIVE`
- `GET /sensors?roomId=LIB-301`

This makes it clearer to clients that they are querying a collection, not navigating a nested resource tree.

### Part 4: Deep Nesting with Sub-Resources

#### 4.1 Benefits of the Sub-Resource Locator pattern (Question)

The sub-resource locator pattern is valuable because it keeps the API structure aligned with the real-world model while keeping implementation complexity under control. In this project, `SensorResource` is responsible for the sensor collection (`/sensors`) and then delegates nested reading routes to `SensorReadingResource` under `/sensors/{sensorId}/readings`.

Architectural benefits include:

- **Separation of concerns**: sensor registration/filtering logic stays in one class, and reading-history logic stays in another.
- **Maintainability**: each class remains smaller and easier to test and update.
- **Clear nesting**: the code structure mirrors the URI structure, which is easier to reason about than putting every nested route inside one large “controller” class.

In larger APIs, this delegation approach prevents a “god resource class” that contains unrelated responsibilities and becomes difficult to maintain.

#### 4.2 Reading POST side effect on parent sensor `currentValue` (Question)

When the client posts a new reading to `/sensors/{sensorId}/readings`, the service performs two linked actions:

1. **Appends the reading to the sensor’s historical log** (so the API can return the full history later).
2. **Updates the parent sensor’s `currentValue`** to match the newest reading.

This side effect is intentional and important for data consistency. It ensures that the “summary view” of a sensor (its most recent value) is always consistent with the last reading in the history list. Without this, the API could return a stale `currentValue` even after new readings are submitted.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 5.2 Why 422 is semantically accurate for missing linked `roomId` (Question)

When a client posts a new sensor, the endpoint `/sensors` exists and the JSON payload can be syntactically valid. The failure occurs because the payload references a related entity (`roomId`) that does not exist in the system. That is not the same as a missing URL path.

That is why **`422 Unprocessable Entity`** is more semantically accurate than `404` in this case:

- `404 Not Found` typically means “the URL resource you requested does not exist.”
- `422` means “the server understands the payload, but cannot process it because it violates domain rules.”

In other words, the request is well-formed, but it fails **semantic validation** due to a broken dependency link.

#### 5.4 Cybersecurity risks of exposing stack traces (Question)

Returning raw Java stack traces to API consumers is a security and professional-practice risk. A stack trace can unintentionally reveal:

- Internal package and class names (useful for reverse-engineering the codebase structure),
- Framework and library details (which can hint at specific versions and known vulnerabilities),
- File paths and server environment details,
- The exact failure location and logic path, which can help attackers craft targeted inputs.

From a cybersecurity standpoint, this is an information disclosure problem. A safer design is to log full error details on the server (so developers can debug) but return only a generic message to the client. That is why this API includes a global `ExceptionMapper<Throwable>` which returns a clean JSON `500` response and keeps technical details in server logs only.

#### 5.5 Why use JAX-RS filters for logging cross-cutting concerns? (Question)

Logging is a cross-cutting concern: it applies to every endpoint in a consistent way. Implementing logging with JAX-RS filters is advantageous because:

- **Consistency**: every request/response is logged with the same format and fields (method, URI, status).
- **Maintainability**: you avoid repeating `Logger.info(...)` inside every single resource method, which becomes messy and error-prone.
- **Separation of concerns**: resource classes remain focused on business logic (rooms, sensors, readings), while the filter handles observability.

This approach is closer to how real production APIs are built: centralized logging improves debugging and monitoring without polluting endpoint code with repeated boilerplate.
