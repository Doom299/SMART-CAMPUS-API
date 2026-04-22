package com.smartcampus.api.config;

import com.smartcampus.api.filter.ApiLoggingFilter;
import com.smartcampus.api.resource.DiscoveryResource;
import com.smartcampus.api.resource.RoomResource;
import com.smartcampus.api.resource.SensorResource;
import com.smartcampus.api.exception.GlobalExceptionMapper;
import com.smartcampus.api.exception.IllegalArgumentExceptionMapper;
import com.smartcampus.api.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.exception.SensorUnavailableExceptionMapper;
import com.smartcampus.api.exception.WebApplicationExceptionMapper;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(IllegalArgumentExceptionMapper.class);
        classes.add(WebApplicationExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(ApiLoggingFilter.class);
        return classes;
    }
}
