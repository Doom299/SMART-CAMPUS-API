package com.smartcampus.api.resource;

import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.payload.ApiMessage;
import com.smartcampus.api.store.InMemoryDataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    @GET
    public List<Room> getRooms() {
        return new ArrayList<Room>(InMemoryDataStore.ROOMS.values());
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Room id is required.");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than 0.");
        }
        if (InMemoryDataStore.ROOMS.containsKey(room.getId())) {
            throw new ClientErrorException("Room with id " + room.getId() + " already exists.",
                    Response.Status.CONFLICT);
        }
        InMemoryDataStore.ROOMS.put(room.getId(), room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location)
                .entity(room)
                .build();
    }

    @GET
    @Path("/{id}")
    public Room getRoomById(@PathParam("id") String id) {
        Room room = InMemoryDataStore.ROOMS.get(id);
        if (room == null) {
            throw new NotFoundException("Room with id " + id + " was not found.");
        }
        return room;
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = InMemoryDataStore.ROOMS.get(id);
        if (room == null) {
            throw new NotFoundException("Room with id " + id + " was not found.");
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + id + " cannot be deleted because it still has assigned sensors.");
        }

        InMemoryDataStore.ROOMS.remove(id);
        return Response.ok(new ApiMessage("Room " + id + " deleted successfully.")).build();
    }
}
