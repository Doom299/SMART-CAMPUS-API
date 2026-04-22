package com.smartcampus.api.resource;

import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import com.smartcampus.api.store.InMemoryDataStore;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        Sensor sensor = InMemoryDataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with id " + sensorId + " was not found.");
        }
        return InMemoryDataStore.getOrCreateReadings(sensorId);
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = InMemoryDataStore.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with id " + sensorId + " was not found.");
        }
        if (reading == null) {
            throw new IllegalArgumentException("Reading payload is required.");
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is in MAINTENANCE and cannot accept new readings.");
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        InMemoryDataStore.getOrCreateReadings(sensorId).add(reading);
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
