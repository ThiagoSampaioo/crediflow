package com.crediflow.controller;

import com.crediflow.dto.PixKeyRequestDTO;
import com.crediflow.dto.PixKeyResponseDTO;
import com.crediflow.entity.PixKey;
import com.crediflow.enums.PixKeyType;
import com.crediflow.service.PixKeyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/pix-keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PixKeyController {

    @Inject
    PixKeyService service;

    @POST
    public Response create(PixKeyRequestDTO dto) {
        PixKey pixKey = service.registerPixKey(dto.bankAccountId, dto.pixKey, PixKeyType.valueOf(dto.keyType));
        return Response.status(Response.Status.CREATED).entity(toResponseDTO(pixKey)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        PixKey pixKey = service.getById(id);
        return Response.ok(toResponseDTO(pixKey)).build();
    }

    @GET
    @Path("/account/{accountId}")
    public Response getAllByAccount(@PathParam("accountId") Long accountId) {
        List<PixKeyResponseDTO> keys = service.getAllByAccount(accountId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return Response.ok(keys).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, PixKeyRequestDTO dto) {
        PixKey pixKey = service.updatePixKey(id, dto.bankAccountId, dto.pixKey, PixKeyType.valueOf(dto.keyType));
        return Response.ok(toResponseDTO(pixKey)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.deletePixKey(id);
        return Response.noContent().build();
    }

    // Método auxiliar para conversão
    private PixKeyResponseDTO toResponseDTO(PixKey pixKey) {
        PixKeyResponseDTO dto = new PixKeyResponseDTO();
        dto.id = pixKey.getId();
        dto.pixKey = pixKey.getKey();
        dto.keyType = pixKey.getKeyType().name();
        dto.bankAccountId = pixKey.getBankAccount().getId();
        dto.createdAt = pixKey.getCreatedAt();
        return dto;
    }
}
