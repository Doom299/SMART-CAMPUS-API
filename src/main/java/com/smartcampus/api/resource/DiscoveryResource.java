package com.smartcampus.api.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Map<String, Object> getDiscovery() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("apiName", "Smart Campus REST API");
        response.put("version", "v1");
        response.put("contact", "facilities@university.local");

        Map<String, String> links = new LinkedHashMap<String, String>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);
        return response;
    }
}
