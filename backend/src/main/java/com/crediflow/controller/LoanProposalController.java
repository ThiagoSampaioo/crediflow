package com.crediflow.controller;

import com.crediflow.dto.LoanProposalRequestDTO;
import com.crediflow.dto.LoanSimulationResultDTO;
import com.crediflow.dto.LoanProposalResponseDTO;
import com.crediflow.dto.PagedResult;
import com.crediflow.dto.PayInstallmentRequestDTO;
import com.crediflow.entity.LoanProposal;
import com.crediflow.repository.LoanProposalRepository;
import com.crediflow.service.CCBService;
import com.crediflow.service.LoanProposalService;
import com.crediflow.service.TransactionService;
import com.crediflow.util.LoanSimulationUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("/loan-proposals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Loan Proposals", description = "Gerenciamento de propostas de crédito consignado")
public class LoanProposalController {

    @Inject
    LoanProposalService proposalService;

    @Inject
    LoanProposalRepository loanProposalRepository;

    @Inject
    CCBService ccbService;

    @Inject
    TransactionService transactionService;

    @GET
    @Operation(summary = "Listar todas as propostas")
    @RolesAllowed("admin")
    public List<LoanProposalResponseDTO> listAll() {
        return proposalService.listAll();
    }

    @GET
    @Path("/paged")
    @RolesAllowed({ "admin", "client" })
    @Operation(summary = "Listar propostas com paginação e filtro por nome/cpf/email")
    public PagedResult<LoanProposalResponseDTO> listPaged(
            @QueryParam("search") @DefaultValue("") String search,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return proposalService.findAllPaged(search, page, size);
    }

    @GET
    @Path("/my-proposals")
    @RolesAllowed("client")
    @Operation(summary = "Listar propostas do cliente logado com paginação")
    public PagedResult<LoanProposalResponseDTO> getProposalsForLoggedCustomer(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return proposalService.findAllByLoggedCustomer(page, size);
    }

    @POST
    @Path("/simulate")
    @Transactional
    @RolesAllowed({ "admin", "client" })
    @Operation(summary = "Simular uma nova proposta de crédito (sem persistir)")
    public LoanSimulationResultDTO simulate(@Valid LoanProposalRequestDTO dto) {
        return proposalService.simulateDetailed(dto);
    }

    @POST
    @Transactional
    @RolesAllowed({ "admin", "client" })
    @Operation(summary = "Criar uma nova proposta de crédito")
    public LoanSimulationResultDTO create(@Valid LoanProposalRequestDTO dto) {
        return proposalService.createWithSimulation(dto);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("admin")
    public LoanProposalResponseDTO update(@PathParam("id") Long id, LoanProposalRequestDTO dto) {
        return proposalService.updateProposal(id, dto);
    }

    @PUT
    @Path("/{id}/sign")
    @Transactional
    @Operation(summary = "Confirmar assinatura da proposta")
    @RolesAllowed({ "admin", "company, client" })
    public LoanProposalResponseDTO confirmSignature(@PathParam("id") Long id) {
        return proposalService.toDTO(proposalService.confirmSignature(id));
    }

   /* @PUT
    @Path("/{id}/review")
    @Transactional
    @Operation(summary = "Analisar proposta (aprovar ou rejeitar)")
    @RolesAllowed("admin")
    public LoanProposalResponseDTO review(
            @PathParam("id") Long id,
            @QueryParam("approved") boolean approved) {
        return proposalService.toDTO(proposalService.reviewProposal(id, approved));
    }*/

    @PUT
    @Path("/{id}/pay")
    @Transactional
    @Operation(summary = "Marcar proposta como paga")
    @RolesAllowed("admin")
    public LoanProposalResponseDTO pay(@PathParam("id") Long id) {
        return proposalService.toDTO(proposalService.markAsPaid(id));
    }

    @PUT
    @Path("/{id}/cancel")
    @Transactional
    @Operation(summary = "Marcar proposta como cancelada")
    @RolesAllowed("admin")
    public LoanProposalResponseDTO cancel(@PathParam("id") Long id) {
        return proposalService.toDTO(proposalService.markAsCanceled(id));
    }

    // Método para pagar uma parcela específica
    @POST
    @Path("/installments/pay")
    @RolesAllowed({ "client", "admin" })
    @Operation(summary = "Pagar uma parcela específica de uma proposta")
    public Response payInstallment(PayInstallmentRequestDTO dto) {
        proposalService.payInstallment(dto.proposalId, dto.installmentNumber, dto.bankAccountId);
        return Response.ok().entity("Parcela paga com sucesso").build();
    }

    // Método para pagar uma parcela e retornar o comprovante em PDF
    @POST
    @Path("/installments-pay/{installmentId}/pay/{accountId}/receipt")
    @Produces("application/pdf")
    @RolesAllowed({ "client", "admin" })
    @Operation(summary = "Pagar parcela e retornar comprovante em PDF")
    public Response payWithReceipt(@PathParam("installmentId") Long installmentId,
            @PathParam("accountId") Long bankAccountId) {
        byte[] pdf = transactionService.payInstallmentAndReturnReceipt(installmentId, bankAccountId);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=comprovante.pdf")
                .build();
    }

    @GET
    @Path("/{id}/ccb-generate")
    @Produces("application/pdf")
    @RolesAllowed({ "admin", "company", "client" })
    @Operation(summary = "Gerar CCB (Contrato de Cessão de Crédito) em PDF")
    @Transactional
    public Response gerarCCB(@PathParam("id") Long id) {
        LoanProposal proposal = loanProposalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Proposta não encontrada"));

        proposal.getInstallments().size(); // força carregar as parcelas

        byte[] pdf = ccbService.gerarPdfCCB(proposal);

        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=ccb_proposta_" + id + ".pdf")
                .build();
    }

    @GET
    @Path("/{id}/ccb")
    @Produces("application/pdf")
    @RolesAllowed({ "admin", "company", "client" })
    @Operation(summary = "Obter detalhes da CCB (Contrato de Cessão de Crédito)")
    public Response getCCBDetails(@PathParam("id") Long id) {
        LoanProposal proposal = loanProposalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Proposta não encontrada"));
        proposal.getInstallments().size(); // força carregar as parcelas

        byte[] pdf = ccbService.getCCBDetails(proposal);

        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=ccb_proposta_" + id + ".pdf")
                .build();
    }

    @GET
    @Path("/{id}/ccb-sign")
    @Produces("application/pdf")
    @RolesAllowed({ "admin", "company", "client" })
    @Operation(summary = "Assinar CCB (Contrato de Cessão de Crédito)")
    @Transactional
    public Response signCCB(@PathParam("id") Long id) {
        LoanProposal proposal = loanProposalRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Proposta não encontrada"));
        proposal.getInstallments().size(); // força carregar as parcelas

        byte[] pdf = ccbService.signCCB(proposal);

        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=ccb_proposta_" + id + ".pdf")
                .build();
    }

    @GET
    @Path("/available-margin/{customerId}")
    @Operation(summary = "Obter margem disponível para o cliente")
    @RolesAllowed({ "admin", "client" })
    public BigDecimal getMargin(@PathParam("customerId") Long customerId) {
        return proposalService.getAvailableMargin(customerId);
    }

}
