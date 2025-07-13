package com.crediflow.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class KeycloakConfig {

    @ConfigProperty(name = "keycloak.admin.server-url")
    String serverUrl;

    @ConfigProperty(name = "keycloak.admin.realm")
    String realm;

    @ConfigProperty(name = "keycloak.admin.client-id")
    String clientId;

    @ConfigProperty(name = "keycloak.admin.client-secret")
    String clientSecret;

    public String serverUrl() {
        return serverUrl;
    }

    public String realm() {
        return realm;
    }

    public String clientId() {
        return clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }
}
