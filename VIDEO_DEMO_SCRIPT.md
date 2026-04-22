# 10-Minute Video Demo Script

## 0:00 - 1:00 Intro
- Introduce yourself and module/coursework title.
- State technology constraints followed: Java + JAX-RS (Jersey), Maven, in-memory data only.
- Show project folder and highlight key files (`pom.xml`, resources, exception mappers, filters, `README.md`).

## 1:00 - 2:00 Build And Run
- Run `mvn clean package`.
- Show generated WAR in `target`.
- Deploy to Tomcat and start server.
- Open browser or Postman at `GET /api/v1` discovery endpoint.

## 2:00 - 4:00 Room Endpoints
- `POST /rooms` create one room.
- `GET /rooms` show list.
- `GET /rooms/{id}` show specific room.
- Explain delete business rule before testing delete.

## 4:00 - 6:00 Sensor Endpoints
- `POST /sensors` with valid `roomId` (success).
- `GET /sensors` show all sensors.
- `GET /sensors?type=CO2` show filtering.
- `POST /sensors` with invalid `roomId` to show `422`.

## 6:00 - 8:00 Sub-Resource Readings
- `POST /sensors/{sensorId}/readings` add reading.
- `GET /sensors/{sensorId}/readings` show history list.
- Show that `Sensor.currentValue` updates after reading POST.
- Optional negative case: sensor with status `MAINTENANCE` then POST reading returns `403`.

## 8:00 - 9:00 Error Handling
- Attempt deleting room with assigned sensors to trigger `409`.
- Trigger a controlled server error scenario (if prepared) and show generic `500` body without stack trace.

## 9:00 - 10:00 Logging + Close
- Show console logs from filter:
  - incoming method + URI
  - outgoing status code
- Close by confirming all required tasks: setup/discovery, rooms, sensors/filter, sub-resource, error handling/logging.
