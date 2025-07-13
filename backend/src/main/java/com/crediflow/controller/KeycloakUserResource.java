package com.crediflow.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import com.crediflow.service.KeycloakAdminService;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeycloakUserResource {

    @Inject
    KeycloakAdminService keycloakService;

    @GET
    public Response getAllUsers() {
        try {
            List<Map<String, Object>> users = keycloakService.listUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao buscar usuários").build();
        }
    }

    @GET
    @Path("/role/{role}")
    public Response getUsersByRole(@PathParam("role") String role) {
        try {
            List<Map<String, Object>> users = keycloakService.listUsersByRole(role);
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao buscar usuários por role").build();
        }
    }

    @POST
    @Path("/with-role/{role}")
    public Response createUserWithRole(@PathParam("role") String role, Map<String, Object> userData) {
        try {
            keycloakService.createUser(userData, role);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Erro ao criar usuário com role").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updateUser(@PathParam("id") String userId, Map<String, Object> userData) {
        try {
            keycloakService.updateUser(userId, userData);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao atualizar usuário").build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id") String userId) {
        try {
            keycloakService.deleteUser(userId);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError().entity("Erro ao deletar usuário").build();
        }
    }

}
