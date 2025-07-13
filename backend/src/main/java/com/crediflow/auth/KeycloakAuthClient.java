package com.crediflow.auth;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;

@Path("/realms/crediflow/protocol/openid-connect")
@RegisterRestClient(configKey = "keycloak-auth")
public interface KeycloakAuthClient {

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> authenticate(Form form);
}
