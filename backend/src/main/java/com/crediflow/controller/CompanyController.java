package com.crediflow.controller;

import com.crediflow.dto.CompanyRequestDTO;
import com.crediflow.dto.CompanyResponseDTO;
import com.crediflow.dto.CustomerResponseDTO;
import com.crediflow.dto.PagedResult;
import com.crediflow.entity.Company;
import com.crediflow.service.CompanyService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;
import java.util.stream.Collectors;

@Path("/companies")
@RolesAllowed({"admin", "company"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Companies", description = "Gerenciamento de empresas conveniadas")
public class CompanyController {

    @Inject
    CompanyService companyService;

    @POST
    @Transactional
    @Operation(summary = "Criar nova empresa conveniada")
    @APIResponse(responseCode = "201", description = "Empresa criada com sucesso")
    public Response create(@Valid CompanyRequestDTO dto) {
        Company company = companyService.fromDTO(dto);
        Company saved = companyService.create(company);
        return Response.status(Response.Status.CREATED).entity(companyService.toDTO(saved)).build();
    }

    @GET
    @Operation(summary = "Listar empresas com paginação")
    public PagedResult<CompanyResponseDTO> findAll(
            @QueryParam("page") @DefaultValue("0") @Parameter(description = "Número da página (começa em 0)") int page,
            @QueryParam("size") @DefaultValue("10") @Parameter(description = "Quantidade de itens por página") int size) {
        return companyService.findAllPaged(page, size);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar empresa por ID")
    @APIResponse(responseCode = "200", description = "Empresa encontrada")
    @APIResponse(responseCode = "404", description = "Empresa não encontrada")
    public Response getById(@PathParam("id") Long id) {
        return companyService.getById(id)
                .map(company -> Response.ok(companyService.toDTO(company)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/search")
    @Operation(summary = "Buscar empresa por nome, CNPJ ou e-mail com paginação")
    public PagedResult<CompanyResponseDTO> search(
            @QueryParam("query") String query,
            @DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("size") int size) {
        return companyService.search(query != null ? query : "", page, size);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualizar dados da empresa")
    @APIResponse(responseCode = "200", description = "Empresa atualizada")
    @APIResponse(responseCode = "404", description = "Empresa não encontrada")
    public Response update(@PathParam("id") Long id, @Valid CompanyRequestDTO dto) {
        return companyService.getById(id)
                .map(company -> {
                    companyService.updateEntity(company, dto);
                    return Response.ok(companyService.toDTO(company)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remover empresa do sistema")
    @APIResponse(responseCode = "204", description = "Empresa removida")
    @APIResponse(responseCode = "404", description = "Empresa não encontrada")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = companyService.delete(id);
        return deleted
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @GET
    @Path("/keycloak/{keycloakId}")
    @Operation(summary = "Buscar empresa por ID do Keycloak")
    public Response getByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        return companyService.findByKeycloakId(keycloakId)
                .map(company -> Response.ok(companyService.toDTO(company)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
