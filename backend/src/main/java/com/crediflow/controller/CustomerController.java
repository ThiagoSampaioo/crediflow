package com.crediflow.controller;

import com.crediflow.dto.CustomerRequestDTO;
import com.crediflow.dto.CustomerResponseDTO;
import com.crediflow.dto.PagedResult;
import com.crediflow.entity.Customer;
import com.crediflow.service.CustomerService;
import com.crediflow.service.KeycloakAdminService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/customers")
// admin e company podem acessar
@RolesAllowed({ "admin", "company" })

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customers", description = "Gerenciamento de clientes conveniados")
public class CustomerController {

    @Inject
    CustomerService customerService;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @POST
    @Transactional
    @Operation(summary = "Criar novo cliente")
    @APIResponse(responseCode = "201", description = "Cliente criado com sucesso")
    public Response create(@Valid CustomerRequestDTO dto) {
        Customer customer = customerService.fromDTO(dto);
        Customer saved = customerService.create(customer);
        return Response.status(Response.Status.CREATED).entity(customerService.toDTO(saved)).build();
    }

    @GET
    @Operation(summary = "Listar todos os clientes")
    public List<CustomerResponseDTO> findAll() {
        return customerService.findAll()
                .stream()
                .map(customerService::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/paged")
    @Operation(summary = "Listar clientes paginados")
    public PagedResult<CustomerResponseDTO> findPaged(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return customerService.findAllPaged(page, size);
    }

    @GET
    @Path("/search")
    @Operation(summary = "Buscar clientes por nome, CPF ou e-mail com paginação")
    public PagedResult<CustomerResponseDTO> search(
            @QueryParam("query") String query,
            @DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("size") int size) {
        return customerService.search(query != null ? query : "", page, size);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @APIResponse(responseCode = "200", description = "Cliente encontrado")
    @APIResponse(responseCode = "404", description = "Cliente não encontrado")
    public Response getById(@PathParam("id") Long id) {
        return customerService.getById(id)
                .map(customer -> Response.ok(customerService.toDTO(customer)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualizar cliente")
    @APIResponse(responseCode = "200", description = "Cliente atualizado com sucesso")
    @APIResponse(responseCode = "404", description = "Cliente não encontrado")
    public Response update(@PathParam("id") Long id, @Valid CustomerRequestDTO dto) {
        return customerService.getById(id)
                .map(customer -> {
                    customerService.updateEntity(customer, dto);
                    return Response.ok(customerService.toDTO(customer)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Excluir cliente")
    @APIResponse(responseCode = "204", description = "Cliente removido com sucesso")
    @APIResponse(responseCode = "404", description = "Cliente não encontrado")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = customerService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    // Buscar clientes por ID da empresa (Keycloak ID) com paginacao
    @GET
    @Path("/company/{keycloakId}")
    @Operation(summary = "Buscar clientes por ID da empresa")
    @APIResponse(responseCode = "200", description = "Clientes encontrados")
    @APIResponse(responseCode = "404", description = "Empresa não encontrada")
    public PagedResult<CustomerResponseDTO> getByCompanyId(
            @PathParam("keycloakId") String keycloakId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return customerService.findByCompanyKeycloakId(keycloakId, page, size);
    }

    // buscar cliente por ID do Keycloak
    @GET
    @Path("/keycloak/{keycloakId}")
     @RolesAllowed({ "client" })
    @Operation(summary = "Buscar cliente por ID do Keycloak")
    @APIResponse(responseCode = "200", description = "Cliente encontrado")
    @APIResponse(responseCode = "404", description = "Cliente não encontrado")
    public Response getByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        return customerService.findByKeycloakId(keycloakId)
                .map(customer -> Response.ok(customerService.toDTO(customer)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }




}
