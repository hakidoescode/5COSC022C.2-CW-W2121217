/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.smartcampusapi.resource;

import com.smartcampus.smartcampusapi.exception.SensorUnavailableException;
import com.smartcampus.smartcampusapi.model.Sensor;
import com.smartcampus.smartcampusapi.model.SensorReading;
import com.smartcampus.smartcampusapi.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        if (!store.getSensors().containsKey(sensorId)) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Sensor not found: " + sensorId);
            return Response.status(404).entity(err).build();
        }
        List<SensorReading> list = store.getReadings().getOrDefault(sensorId, List.of());
        return Response.ok(list).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Sensor not found: " + sensorId);
            return Response.status(404).entity(err).build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }
        SensorReading newReading = new SensorReading(reading.getValue());
        store.getReadings().get(sensorId).add(newReading);
        sensor.setCurrentValue(reading.getValue()); // update parent sensor
        return Response.status(201).entity(newReading).build();
    }
}
