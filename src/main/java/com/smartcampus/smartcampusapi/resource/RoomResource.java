/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.smartcampusapi.resource;

import com.smartcampus.smartcampusapi.exception.RoomNotEmptyException;
import com.smartcampus.smartcampusapi.model.Room;
import com.smartcampus.smartcampusapi.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        return Response.ok(store.getRooms().values()).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Room ID is required");
            return Response.status(400).entity(err).build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Room already exists: " + room.getId());
            return Response.status(409).entity(err).build();
        }
        store.getRooms().put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Room not found: " + roomId);
            return Response.status(404).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Room not found: " + roomId);
            return Response.status(404).entity(err).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}