package com.crediflow.controller;

import com.crediflow.dto.PagedResult;
import com.crediflow.dto.TransactionRequestDTO;
import com.crediflow.dto.TransactionResponseDTO;
import com.crediflow.entity.BankAccount;
import com.crediflow.entity.Transaction;
import com.crediflow.enums.TransactionStatus;
import com.crediflow.enums.TransactionType;
import com.crediflow.repository.TransactionRepository;
import com.crediflow.service.TransactionPDFService;
import com.crediflow.service.TransactionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionService transactionService;

    @Inject
    TransactionPDFService transactionPDFService;

    @Inject
    TransactionRepository transactionRepository;

    @GET
    public Response listAll() {
        List<TransactionResponseDTO> transactions = transactionService.listAll().stream()
                .map(TransactionResponseDTO::fromEntity).collect(Collectors.toList());
        return Response.ok(transactions).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Transaction tx = transactionService.getById(id);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @GET
    @Path("/by-account/{accountId}")
    public Response getTransactionsPaged(
            @PathParam("accountId") Long accountId,
            @QueryParam("type") TransactionType type,
            @QueryParam("status") TransactionStatus status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        PagedResult<TransactionResponseDTO> result = transactionService.searchTransactions(accountId, type, status,
                page, size);
        return Response.ok(result).build();
    }

    @POST
    @Path("/transfer")
    public Response transfer(TransactionRequestDTO dto) {
        Transaction tx = transactionService.transfer(
                dto.fromAccountId,
                dto.toAccountId,
                new BigDecimal(dto.amount),
                TransactionType.valueOf(dto.type),
                dto.description);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @POST
    @Path("/deposit")
    public Response deposit(TransactionRequestDTO dto) {
        Transaction tx = transactionService.deposit(
                dto.toAccountId,
                new BigDecimal(dto.amount),
                dto.description);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @POST
    @Path("/payment")
    public Response pay(TransactionRequestDTO dto) {
        Transaction tx = transactionService.pay(
                dto.fromAccountId,
                new BigDecimal(dto.amount),
                dto.description,
                TransactionType.valueOf(dto.type));
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @GET
    @Path("/receipt/{id}")
    @Produces("application/pdf")
    public Response getReceipt(@PathParam("id") Long id) {
        Transaction tx = transactionService.getById(id);
        if (tx == null) {
            throw new NotFoundException("Transação não encontrada heheh");
        }
        byte[] pdf = transactionPDFService.generateReceipt(tx);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=comprovante.pdf")
                .build();

    }

    @POST
    @Path("/transfer/agency")
    public Response transferByAgency(TransactionRequestDTO dto) {
        Transaction tx = transactionService.transferByAgencyAccount(
                dto.fromAccountId,
                dto.agencyNumber,
                dto.accountNumber,
                new BigDecimal(dto.amount),
                dto.description);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @POST
    @Path("/deposit/agency")
    public Response depositByAgency(TransactionRequestDTO dto) {
        Transaction tx = transactionService.depositByAgencyAccount(
                dto.agencyNumber, dto.accountNumber, new BigDecimal(dto.amount), dto.description);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

    @POST
    @Path("/pix")
    public Response pixTransfer(TransactionRequestDTO dto) {
        Transaction tx = transactionService.pixTransfer(
                dto.fromAccountId, dto.pixKey, new BigDecimal(dto.amount), dto.description);
        return Response.ok(TransactionResponseDTO.fromEntity(tx)).build();
    }

   

}
