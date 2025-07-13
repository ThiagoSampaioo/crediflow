package com.crediflow.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class KeycloakService {

    @Inject
    KeycloakAdminService userClient;

    public Optional<Map<String, Object>> getUserById(String userId) {
        try {
            Map<String, Object> user = userClient.getUserById(userId);
            return Optional.of(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
