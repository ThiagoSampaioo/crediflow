package com.crediflow.service;

import com.crediflow.config.KeycloakConfig;
import com.crediflow.exception.ConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;

import java.util.*;

@ApplicationScoped
public class KeycloakAdminService {

    @Inject
    ObjectMapper mapper;

    @Inject
    KeycloakConfig config;

    public String getAdminToken() throws Exception {
        var client = ClientBuilder.newClient();

        var form = new Form()
                .param("client_id", config.clientId())
                .param("client_secret", config.clientSecret())
                .param("grant_type", "client_credentials");

        var response = client
                .target(config.serverUrl() + "/realms/" + config.realm() + "/protocol/openid-connect/token")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        String responseBody = response.readEntity(String.class);

        if (response.getStatus() != 200) {
            throw new ConflictException("Erro ao obter token de administrador: " + responseBody);
        }

        Map<String, Object> json = mapper.readValue(responseBody, Map.class);
        return (String) json.get("access_token");
    }

    public List<Map<String, Object>> listUsers() throws Exception {
        String token = getAdminToken();

        var client = ClientBuilder.newClient();
        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        return mapper.readValue(response.readEntity(String.class), List.class);
    }

    public List<Map<String, Object>> listUsersByRole(String roleName) {
        try {
            String token = getAdminToken();

            var client = ClientBuilder.newClient();
            var response = client
                    .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/roles/" + roleName + "/users")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get();

            if (response.getStatus() != 200) {
                return List.of();
            }

            return mapper.readValue(response.readEntity(String.class), List.class);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void createUser(Map<String, Object> userData, String roleName) throws Exception {
        String token = getAdminToken();
        var client = ClientBuilder.newClient();

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", userData.get("username"));
        payload.put("firstName", userData.get("firstName"));
        payload.put("lastName", userData.get("lastName"));
        payload.put("email", userData.get("email"));
        payload.put("enabled", true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userData.get("password"));
        credentials.put("temporary", false);

        payload.put("credentials", List.of(credentials));

        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(payload));

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar usuário: " + response.readEntity(String.class));
        }

        if (roleName != null && !roleName.isBlank()) {
            String location = response.getHeaderString("Location");
            String userId = location.substring(location.lastIndexOf("/") + 1);

            Map<String, Object> role = getRealmRole(roleName, token);

            var assignResponse = client
                    .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId
                            + "/role-mappings/realm")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(List.of(role)));

            if (assignResponse.getStatus() != 204) {
                throw new RuntimeException("Erro ao atribuir role: " + assignResponse.readEntity(String.class));
            }
        }
    }

    public void updateUser(String userId, Map<String, Object> userData) throws Exception {
        String token = getAdminToken();

        var client = ClientBuilder.newClient();

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", userData.get("username"));
        payload.put("firstName", userData.get("firstName"));
        payload.put("lastName", userData.get("lastName"));
        payload.put("email", userData.get("email"));
        payload.put("enabled", true);

        if (userData.containsKey("password")) {
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("type", "password");
            credentials.put("value", userData.get("password"));
            credentials.put("temporary", false);
            payload.put("credentials", List.of(credentials));
        }

        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(payload));

        if (response.getStatus() >= 400) {
            throw new RuntimeException("Erro ao atualizar usuário: " + response.readEntity(String.class));
        }
    }

    public void deleteUser(String userId) throws Exception {
        String token = getAdminToken();

        var client = ClientBuilder.newClient();

        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request()
                .header("Authorization", "Bearer " + token)
                .delete();

        if (response.getStatus() >= 400) {
            throw new RuntimeException("Erro ao deletar usuário: " + response.readEntity(String.class));
        }
    }

    public Map<String, Object> getUserById(String userId) throws Exception {
        String token = getAdminToken();

        var client = ClientBuilder.newClient();
        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("Erro ao buscar usuário: " + response.readEntity(String.class));
        }

        return mapper.readValue(response.readEntity(String.class), Map.class);
    }

    public String createUserAndReturnId(Map<String, Object> userData, String roleName) throws Exception {
        String token = getAdminToken();
        var client = ClientBuilder.newClient();

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", userData.get("username"));
        payload.put("firstName", userData.get("firstName"));
        payload.put("lastName", userData.get("lastName"));
        payload.put("email", userData.get("email"));
        payload.put("enabled", true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userData.get("password"));
        credentials.put("temporary", false);
        payload.put("credentials", List.of(credentials));

        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(payload));

        if (response.getStatus() != 201) {
            throw new RuntimeException("Erro ao criar usuário: " + response.readEntity(String.class));
        }

        String location = response.getHeaderString("Location");
        String userId = location.substring(location.lastIndexOf("/") + 1);

        if (roleName != null && !roleName.isBlank()) {
            Map<String, Object> role = getRealmRole(roleName, token);

            var assignResponse = client
                    .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId
                            + "/role-mappings/realm")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(List.of(role)));

            if (assignResponse.getStatus() != 204) {
                throw new RuntimeException("Erro ao atribuir role: " + assignResponse.readEntity(String.class));
            }
        }

        return userId;
    }

    public Optional<Map<String, Object>> getUserByUsername(String username) throws Exception {
        String token = getAdminToken();
        var client = ClientBuilder.newClient();

        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users?username=" + username)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("Erro ao buscar usuário por username: " + response.readEntity(String.class));
        }

        List<Map<String, Object>> users = mapper.readValue(response.readEntity(String.class), List.class);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // 🔧 NOVO MÉTODO UTILITÁRIO
    private Map<String, Object> getRealmRole(String roleName, String token) throws Exception {
        var client = ClientBuilder.newClient();
        var response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/roles/" + roleName)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

        if (response.getStatus() != 200) {
            throw new RuntimeException("Erro ao buscar role '" + roleName + "': " + response.readEntity(String.class));
        }

        return mapper.readValue(response.readEntity(String.class), Map.class);
    }
    public boolean isUserEnabled(String userId) {
    try {
        Map<String, Object> user = getUserById(userId);
        Object enabledValue = user.get("enabled");
        if (enabledValue instanceof Boolean) {
            return (Boolean) enabledValue;
        }
    } catch (Exception e) {
        //  rastrear falhas de consulta ao Keycloak
        e.printStackTrace();
        throw new WebApplicationException("Erro ao verificar status do usuário no Keycloak: " + e.getMessage(), 500);
    }
    return false;
}

public void setUserEnabled(String userId, boolean enabled) throws Exception {
    String token = getAdminToken();
    var client = ClientBuilder.newClient();

    Map<String, Object> payload = new HashMap<>();
    payload.put("enabled", enabled);

    var response = client
        .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
        .request(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token)
        .put(Entity.json(payload));

    if (response.getStatus() >= 400) {
        throw new RuntimeException("Erro ao alterar status do usuário: " + response.readEntity(String.class));
    }
}


}
