package com.crediflow.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ConflictException extends WebApplicationException {
    public ConflictException(String message) {
        super(message, Response.Status.CONFLICT);
    }
}
