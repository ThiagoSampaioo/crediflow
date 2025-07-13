package com.crediflow.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ApiException extends WebApplicationException {
    public ApiException(String message, Response.Status status) {
        super(message, status);
    }
}