package com.crediflow.auth;

import org.jboss.resteasy.reactive.RestForm;

public class KeycloakLoginRequest {

    @RestForm("username")
    public String username;

    @RestForm("password")
    public String password;

    @RestForm("grant_type")
    public String grantType;

    @RestForm("client_id")
    public String clientId;

    @RestForm("client_secret")
    public String clientSecret;

    @RestForm("scope")
    public String scope;
}
