package com.smartcampus.api.resource;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.store.InMemoryDataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor id is required.");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor type is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor roomId is required.");
        }
        if (InMemoryDataStore.SENSORS.containsKey(sensor.getId())) {
            throw new ClientErrorException("Sensor with id " + sensor.getId() + " already exists.",
                    Response.Status.CONFLICT);
        }
        Room room = InMemoryDataStore.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("roomId " + sensor.getRoomId() + " does not exist.");
        }

        InMemoryDataStore.SENSORS.put(sensor.getId(), sensor);
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<String>());
        }
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<Sensor>(InMemoryDataStore.SENSORS.values());
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }

        List<Sensor> filtered = new ArrayList<Sensor>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type)) {
                filtered.add(sensor);
            }
        }
        return filtered;
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
