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
