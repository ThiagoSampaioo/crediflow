package com.crediflow.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class TokenProvider {

    @Inject
    JsonWebToken jwt;

    public String getUserId() {
        return jwt.getSubject();
    }

    public String getEmail() {
        return jwt.getClaim("email");
    }
}
