package com.smartcampus.api.store;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDataStore {
    public static final Map<String, Room> ROOMS = new HashMap<String, Room>();
    public static final Map<String, Sensor> SENSORS = new HashMap<String, Sensor>();
    public static final Map<String, List<SensorReading>> SENSOR_READINGS = new HashMap<String, List<SensorReading>>();

    private InMemoryDataStore() {
    }

    public static List<SensorReading> getOrCreateReadings(String sensorId) {
        List<SensorReading> readings = SENSOR_READINGS.get(sensorId);
        if (readings == null) {
            readings = new ArrayList<SensorReading>();
            SENSOR_READINGS.put(sensorId, readings);
        }
        return readings;
    }
}
