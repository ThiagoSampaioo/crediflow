package com.crediflow.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.crediflow.service.KeycloakAdminService;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    @Inject
    @RestClient
    KeycloakAuthClient keycloakAuthClient;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            String body = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                    + "&grant_type=password"
                    + "&client_id=backend"
                    + "&client_secret=HlO6lkEzgAziWmyefjSAo1aEHhLt1oOc"
                    + "&scope=openid";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/realms/crediflow/protocol/openid-connect/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("STATUS = " + response.statusCode());
            System.out.println("BODY = " + response.body());

            if (response.statusCode() == 200) {
                return Response.ok(response.body()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", response.body()))
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/toggle/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleUserStatus(
            @PathParam("id") String keycloakId,
            @QueryParam("enabled") boolean enabled) {
        try {
            if (keycloakId == null || keycloakId.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID do usuário é obrigatório.").build();
            }

            keycloakAdminService.setUserEnabled(keycloakId, enabled);

            String status = enabled ? "habilitado" : "desabilitado";
            return Response.ok(Map.of(
                    "message", "Usuário " + status + " com sucesso.",
                    "userId", keycloakId,
                    "enabled", enabled)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro ao alterar status do usuário", "details", e.getMessage()))
                    .build();
        }
    }

}
