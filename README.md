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

However, “per-request resources” does **not automatically make the application thread-safe**. In this coursework implementation, the API stores application state in **static in-memory collections** (maps/lists). Those structures are shared by all requests, regardless of how many resource instances are created. Under concurrent load (for example, multiple clients creating rooms and sensors at the same time), unsafe access to shared collections can lead to lost updates, inconsistent reads, or data integrity problems (for example, a room being deleted while a sensor is being linked). To manage this risk in a robust production service, I would protect shared write operations using synchronization/locks and/or thread-safe data structures such as `ConcurrentHashMap` and synchronized lists. For this coursework, I kept the resource classes stateless and centralized shared state in a dedicated store to keep the design simple, predictable, and easy to demonstrate.

#### 1.2 Why Hypermedia/HATEOAS matters (Question)

Hypermedia (HATEOAS: Hypermedia As The Engine Of Application State) is considered an advanced REST characteristic because the server response does not only return data, it also provides **navigation**. In other words, the API tells the client “where to go next” using links.

This benefits client developers because they can discover available resource collections (like rooms and sensors) directly from `GET /api/v1`, instead of depending solely on static documentation. It also reduces coupling between client and server: if paths evolve over time, clients can follow server-provided links rather than hardcoding URLs everywhere. Overall, it makes the API more self-describing, because the response itself becomes a guide for how to interact with the service.

### Part 2: Room Management

#### 2.1 Returning IDs only vs full room objects (Question)

When returning a list of rooms (for example `GET /rooms`), there are two common strategies: returning only IDs, or returning full room objects. IDs-only responses reduce payload size and bandwidth, which matters when collections are very large, but the client then needs extra requests to fetch details (name, capacity), which increases latency and client complexity. Returning full room objects increases payload size, but it is more convenient because the client receives the metadata it needs immediately. For this coursework, returning full room objects is a practical choice because it makes the API easier to use and demonstrate, and the dataset is small and stored in memory.

#### 2.2 Is DELETE idempotent in your implementation? (Question)

In REST, a DELETE operation is considered **idempotent** if repeating the same request multiple times results in the same final server state as performing it once. In this implementation, the first successful `DELETE /rooms/{id}` removes the room from the in-memory store. If the client repeats the exact same delete request afterward, the room no longer exists, so the server returns a “not found” style response. Even though the response code may differ between the first call (success) and later calls (not found), the important point is that the server state after the first deletion is already the final state, and repeating the operation does not remove anything else or create additional changes.

### Part 3: Sensor Operations & Linking

#### 3.1 Effect of wrong Content-Type with `@Consumes(application/json)` (Question)

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the endpoint only accepts JSON request bodies. If a client sends a different media type such as `text/plain` or `application/xml`, JAX-RS will reject the request before the business logic runs, typically returning **`415 Unsupported Media Type`**. This is important because it enforces a clear contract for clients (send JSON) and prevents invalid or ambiguous parsing behavior.

#### 3.2 Why `@QueryParam` is superior for filtering collections (Question)

Using query parameters for filtering (e.g., `GET /sensors?type=CO2`) is generally preferred for collection searching because filters are optional and easy to combine. Query strings naturally represent “search criteria” on a collection, while the base path remains the same resource (`/sensors`). If we encode filters in the path (for example `/sensors/type/CO2`), it starts to look like a fixed resource hierarchy and becomes awkward when more filters are needed (type + status + roomId). With query parameters, the API stays clean and scalable (for example, `GET /sensors?type=CO2&status=ACTIVE`), and it is clearer to clients that they are querying a collection rather than navigating a nested resource tree.

### Part 4: Deep Nesting with Sub-Resources

#### 4.1 Benefits of the Sub-Resource Locator pattern (Question)

The sub-resource locator pattern is valuable because it keeps the API structure aligned with the real-world model while keeping implementation complexity under control. In this project, `SensorResource` is responsible for the sensor collection (`/sensors`) and then delegates nested reading routes to `SensorReadingResource` under `/sensors/{sensorId}/readings`. This separation of concerns keeps each class focused, improves readability, and makes the code easier to maintain as the API grows. It also prevents a single “god” controller class containing every nested route, which becomes difficult to change safely in larger systems.

#### 4.2 Reading POST side effect on parent sensor `currentValue` (Question)

When the client posts a new reading to `/sensors/{sensorId}/readings`, the service appends that reading to the sensor’s historical log and also updates the parent sensor’s `currentValue` to match the newest reading. This side effect is intentional and important for data consistency: it ensures the “summary” value for a sensor always reflects the latest entry in its history. Without this, the API could return a stale `currentValue` even after new readings are accepted.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 5.2 Why 422 is semantically accurate for missing linked `roomId` (Question)

When a client posts a new sensor, the endpoint `/sensors` exists and the JSON payload can be syntactically valid. The failure occurs because the payload references a related entity (`roomId`) that does not exist in the system. That is not the same as a missing URL path.

That is why **`422 Unprocessable Entity`** is more semantically accurate than `404` in this case. `404 Not Found` usually means the URL resource you requested does not exist, while `422` means the server understands the payload but cannot process it because it violates domain rules. In other words, the request is well-formed, but it fails semantic validation due to a broken dependency link.

#### 5.4 Cybersecurity risks of exposing stack traces (Question)

Returning raw Java stack traces to API consumers is a security and professional-practice risk. A stack trace can reveal internal package and class names, framework/library details that may hint at versions, file paths and environment details, and the exact failure location and logic flow. From a cybersecurity standpoint, this is information disclosure: attackers can use these details for reconnaissance and to craft more targeted attacks. A safer design is to log full technical details on the server for debugging, but return only a generic message to the client. That is why this API includes a global `ExceptionMapper<Throwable>` which returns a clean JSON `500` response while keeping technical details in server logs only.

#### 5.5 Why use JAX-RS filters for logging cross-cutting concerns? (Question)

Logging is a cross-cutting concern, meaning it should apply to every endpoint in a consistent way. Implementing logging with JAX-RS filters is advantageous because it guarantees consistent logging for all requests and responses (method, URI, and final status code) without duplicating `Logger.info(...)` calls inside every resource method. It also improves maintainability: if the logging format changes, it is updated in one place. Most importantly, it keeps resource classes focused on business logic while the filter handles observability, which matches common production API design practices.
