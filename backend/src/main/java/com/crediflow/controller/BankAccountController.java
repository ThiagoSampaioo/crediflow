package com.crediflow.controller;

import com.crediflow.dto.AccountRecipientInfoDTO;
import com.crediflow.dto.BankAccountRequestDTO;
import com.crediflow.dto.BankAccountResponseDTO;
import com.crediflow.entity.BankAccount;
import com.crediflow.service.BankAccountService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/bank-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankAccountController {

    @Inject
    BankAccountService service;

    @POST
    public Response create(BankAccountRequestDTO dto) {
        BankAccount account = service.createAccountForCustomer(dto.customerId);
        return Response.status(Response.Status.CREATED).entity(toDTO(account)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        BankAccount account = service.getAccountById(id);
        if (account == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(toDTO(account)).build();
    }

    @GET
    @Path("/by-customer/{customerId}")
    public Response getByCustomer(@PathParam("customerId") Long customerId) {
        BankAccount account = service.getAccountByCustomerId(customerId);
        if (account == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(toDTO(account)).build();
    }

    @GET
    @Path("/by-account-number/{accountNumber}")
    public Response getByAccountNumber(@PathParam("accountNumber") String accountNumber) {
        BankAccount account = service.getAccountByAccountNumber(accountNumber);
        if (account == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(toDTO(account)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.deleteAccount(id);
        return Response.noContent().build();
    }

    @PUT
    public Response update(BankAccountRequestDTO dto) {
        BankAccount existing = service.getAccountByCustomerId(dto.customerId);
        if (existing == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        existing.setAccountNumber(dto.accountNumber);
        existing.setAgencyNumber(dto.agencyNumber);
        BankAccount updated = service.updateAccount(existing);
        return Response.ok(toDTO(updated)).build();
    }

    private BankAccountResponseDTO toDTO(BankAccount entity) {
        BankAccountResponseDTO dto = new BankAccountResponseDTO();
        dto.id = entity.getId();
        dto.accountNumber = entity.getAccountNumber();
        dto.agencyNumber = entity.getAgencyNumber();
        dto.balance = entity.getBalance();
        dto.customerId = entity.getCustomer().getId();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    @GET
    @Path("/account-info/agency")
    public AccountRecipientInfoDTO getByAgencyAndAccount(
            @QueryParam("agency") String agency,
            @QueryParam("account") String account) {
        return service.findRecipientByAgencyAndAccount(agency, account);
    }

    @GET
    @Path("/account-info/pix")
    public AccountRecipientInfoDTO getByPixKey(@QueryParam("key") String key) {
        return service.findRecipientByPixKey(key);
    }

}
