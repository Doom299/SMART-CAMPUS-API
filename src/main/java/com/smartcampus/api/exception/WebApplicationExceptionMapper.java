package com.smartcampus.api.exception;

import com.smartcampus.api.payload.ApiError;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse() != null
                ? exception.getResponse().getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        ApiError error = new ApiError(status, exception.getMessage());
        return Response.status(status)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
