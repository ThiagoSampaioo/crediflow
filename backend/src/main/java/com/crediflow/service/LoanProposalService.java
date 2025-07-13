package com.crediflow.service;

import com.crediflow.dto.*;
import com.crediflow.entity.BankAccount;
import com.crediflow.entity.Company;
import com.crediflow.entity.Customer;
import com.crediflow.entity.LoanInstallment;
import com.crediflow.entity.LoanProposal;
import com.crediflow.entity.Transaction;
import com.crediflow.enums.LoanProposalStatus;
import com.crediflow.enums.TransactionStatus;
import com.crediflow.enums.TransactionType;
import com.crediflow.exception.BadRequestException;
import com.crediflow.repository.BankAccountRepository;
import com.crediflow.repository.CompanyRepository;
import com.crediflow.repository.CustomerRepository;
import com.crediflow.repository.LoanInstallmentRepository;
import com.crediflow.repository.LoanProposalRepository;
import com.crediflow.repository.TransactionRepository;
import com.crediflow.util.LoanSimulationUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class LoanProposalService {

    @Inject
    LoanProposalRepository proposalRepository;

    @Inject
    LoanInstallmentRepository installmentRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    BankAccountRepository bankAccountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    JsonWebToken jwt;

    public List<LoanProposalResponseDTO> listAll() {
        return proposalRepository.listAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<LoanProposal> getById(Long id) {
        return proposalRepository.findByIdOptional(id);
    }

    public PagedResult<LoanProposalResponseDTO> findAllPaged(String search, int page, int size) {
        String likeSearch = "%" + search.toLowerCase() + "%";

        PanacheQuery<LoanProposal> query = proposalRepository
                .find("""
                            LOWER(customer.name) LIKE ?1
                            OR customer.cpf LIKE ?2
                            OR customer.email LIKE ?3
                            ORDER BY customer.id DESC
                        """, likeSearch, search, search)
                .page(Page.of(page, size));

        List<LoanProposalResponseDTO> items = query.list()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResult<>(items, query.count(), page, size);
    }

    public PagedResult<LoanProposalResponseDTO> findAllByLoggedCustomer(int page, int size) {
        // Obtém o ID do usuário logado no token JWT (sub = ID do Keycloak)
        String keycloakId = jwt.getSubject();

        // Busca o customer correspondente
        Customer customer = customerRepository.find("keycloakId", keycloakId)
                .firstResultOptional()
                .orElseThrow(() -> new BadRequestException("Cliente não encontrado"));

        PanacheQuery<LoanProposal> query = proposalRepository
                .find("customer.id = ?1 ORDER BY createdAt DESC", customer.getId())
                .page(Page.of(page, size));

        List<LoanProposalResponseDTO> items = query.list()
                .stream()
                .map(this::toDTO)
                .toList();

        return new PagedResult<>(items, query.count(), page, size);
    }

    @Transactional
    public LoanProposal createProposal(LoanProposalRequestDTO dto) {
        validateDTO(dto);

        Customer customer = customerRepository.findByIdOptional(dto.customerId)
                .orElseThrow(() -> new BadRequestException("Cliente não encontrado"));
        Company company = companyRepository.findByIdOptional(dto.companyId)
                .orElseThrow(() -> new BadRequestException("Empresa não encontrada"));

        LoanProposal proposal = new LoanProposal();
        proposal.setCustomer(customer);
        proposal.setCompany(company);
        proposal.setRequestedAmount(dto.requestedAmount);
        proposal.setAvailableLimit(dto.availableLimit);
        proposal.setTermInMonths(dto.termInMonths);
        proposal.setConvenioType(dto.convenioType);
        proposal.setStatus(LoanProposalStatus.SIMULATED);
        proposal.setCreatedBy(getCurrentUser());
        proposal.setUpdatedBy(getCurrentUser());

        proposalRepository.persist(proposal);
        return proposal;
    }

    public LoanSimulationResultDTO simulateDetailed(LoanProposalRequestDTO dto) {
        validateDTO(dto);
        return LoanSimulationUtil.simulate(dto);
    }

    @Transactional
    public LoanProposal confirmSignature(Long id) {
        LoanProposal proposal = getById(id)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));

        proposal.setStatus(LoanProposalStatus.SIGNED);
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setUpdatedBy(getCurrentUser());

        return proposal;
    }

    @Transactional
    public LoanProposal reviewProposal(Long id, boolean approved) {
        LoanProposal proposal = getById(id)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));

        proposal.setStatus(approved ? LoanProposalStatus.APPROVED : LoanProposalStatus.REJECTED);
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setUpdatedBy(getCurrentUser());

        return proposal;
    }

    @Transactional
    public LoanProposal markAsPaid(Long id) {
        LoanProposal proposal = getById(id)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));

        if (proposal.getStatus() != LoanProposalStatus.SIGNED) {
            throw new BadRequestException("A proposta precisa estar assinada para ser paga");
        }

        BankAccount clientAccount = bankAccountRepository.find(
                "customer.id = ?1", proposal.getCustomer().getId())
                .firstResultOptional()
                .orElseThrow(() -> new BadRequestException("Conta bancária do cliente não encontrada"));

        BigDecimal depositAmount = proposal.getDisbursementValue();
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valor de liberação inválido");
        }

        // Deposita o valor na conta do cliente
        clientAccount.setBalance(clientAccount.getBalance().add(depositAmount));
        bankAccountRepository.persist(clientAccount);

        // Registra transação
        Transaction tx = new Transaction();
        tx.setFromAccount(null); // Origem: sistema
        tx.setToAccount(clientAccount);
        tx.setAmount(depositAmount);
        tx.setType(TransactionType.LOAN_DISBURSEMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Liberação do empréstimo da proposta #" + id);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.persist(tx);

        // Atualiza status
        proposal.setStatus(LoanProposalStatus.PAID);
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setUpdatedBy(getCurrentUser());

        return proposal;
    }

    // marcar proposta como cancelada
    @Transactional
    public LoanProposal markAsCanceled(Long id) {
        LoanProposal proposal = getById(id)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));

        proposal.setStatus(LoanProposalStatus.CANCELED);
        proposal.setUpdatedAt(LocalDateTime.now());
        proposal.setUpdatedBy(getCurrentUser());

        return proposal;
    }

    private void validateDTO(LoanProposalRequestDTO dto) {
        if (dto.requestedAmount == null || dto.availableLimit == null || dto.monthlyInterestRate == null) {
            throw new BadRequestException("Campos obrigatórios não informados");
        }

        if (dto.requestedAmount.compareTo(dto.availableLimit) > 0) {
            throw new BadRequestException("O valor solicitado não pode ser maior que o limite disponível");
        }

        if (dto.termInMonths <= 0 || dto.termInMonths > 96) {
            throw new BadRequestException("O prazo deve ser entre 1 e 96 meses");
        }

        if (dto.convenioType == null || dto.convenioType.isBlank()) {
            throw new BadRequestException("O tipo de convênio não pode ser vazio");
        }

        if (dto.modoSimulacao == null || dto.modoSimulacao.isBlank()) {
            throw new BadRequestException("Modo de simulação é obrigatório");
        }

        if (dto.firstInstallmentDate == null) {
            throw new BadRequestException("Data da primeira parcela obrigatória");
        }

        if (dto.requestedAmount.signum() < 0 || dto.availableLimit.signum() < 0) {
            throw new BadRequestException("Valores não podem ser negativos");
        }

        if (dto.monthlyInterestRate.scale() > 4) {
            throw new BadRequestException("Taxa mensal com até 4 casas decimais");
        }

        if (dto.convenioType.length() > 50) {
            throw new BadRequestException("O tipo de convênio não pode ter mais de 50 caracteres");
        }
    }

    private String getCurrentUser() {
        return securityIdentity != null && !securityIdentity.isAnonymous()
                ? securityIdentity.getPrincipal().getName()
                : "system";
    }

    public LoanProposalResponseDTO toDTO(LoanProposal proposal) {
        LoanProposalResponseDTO dto = new LoanProposalResponseDTO();
        dto.id = proposal.getId();
        dto.customerId = proposal.getCustomer().getId();
        dto.companyId = proposal.getCompany().getId();
        dto.customerName = proposal.getCustomer().getName();
        dto.companyName = proposal.getCompany().getName();
        dto.requestedAmount = proposal.getRequestedAmount();
        dto.availableLimit = proposal.getAvailableLimit();
        dto.termInMonths = proposal.getTermInMonths();
        dto.convenioType = proposal.getConvenioType();
        dto.status = proposal.getStatus();
        dto.createdAt = proposal.getCreatedAt();
        dto.updatedAt = proposal.getUpdatedAt();

        // Novos campos financeiros
        dto.installmentValue = proposal.getInstallmentValue();
        dto.totalPayment = proposal.getTotalPayment();
        dto.financedAmount = proposal.getFinancedAmount();
        dto.monthlyRate = proposal.getMonthlyRate();
        dto.firstInstallmentDate = proposal.getFirstInstallmentDate() != null
                ? proposal.getFirstInstallmentDate().toString()
                : null;
        dto.lastInstallmentDate = proposal.getLastInstallmentDate() != null
                ? proposal.getLastInstallmentDate().toString()
                : null;
        dto.numberOfInstallments = proposal.getNumberOfInstallments();
        dto.grossAmount = proposal.getGrossAmount();
        dto.iofTotal = proposal.getIofTotal();
        dto.iofAnnualRate = proposal.getIofAnnualRate();
        dto.iofAdditionalRate = proposal.getIofAdditionalRate();
        dto.proposalDate = proposal.getProposalDate();
        dto.tacValue = proposal.getTacValue();
        dto.spread = proposal.getSpread();
        dto.effectiveMonthlyRate = proposal.getEffectiveMonthlyRate();
        dto.effectiveAnnualRate = proposal.getEffectiveAnnualRate();
        dto.contractedAnnualRate = proposal.getContractedAnnualRate();
        dto.productType = proposal.getProductType();
        dto.fund = proposal.getFund();
        dto.installmentType = proposal.getInstallmentType();
        dto.disbursementValue = proposal.getDisbursementValue();
        dto.disbursementDate = proposal.getDisbursementDate();
        dto.processingCostTotal = proposal.getProcessingCostTotal();
        dto.amortizationType = proposal.getAmortizationType();
        dto.interestComposition = proposal.getInterestComposition();
        dto.ccb = proposal.getCcb();

        if (proposal.getInstallments() != null) {
            dto.installments = proposal.getInstallments().stream().map(installment -> {
                LoanInstallmentDTO i = new LoanInstallmentDTO();
                i.id = installment.getId();
                i.number = installment.getNumber();
                i.value = installment.getValue();
                i.dueDate = installment.getDueDate();
                i.interest = installment.getInterest();
                i.principal = installment.getPrincipal();
                i.balance = installment.getBalance();
                i.iof = installment.getIof();
                i.additionalValue = installment.getAdditionalValue();
                i.presentValue = installment.getPresentValue();
                i.status = installment.getStatus();
                i.period = installment.getPeriod();
                i.daysElapsed = installment.getDaysElapsed();
                i.daysLate = installment.getDaysLate();
                i.paidValue = installment.getPaidValue();
                i.paid = installment.isPaid();
                return i;
            }).collect(Collectors.toList());
        }

        return dto;
    }

    @Transactional
    public LoanSimulationResultDTO createWithSimulation(LoanProposalRequestDTO dto) {
        validateDTO(dto);

        Customer customer = customerRepository.findByIdOptional(dto.customerId)
                .orElseThrow(() -> new BadRequestException("Cliente não encontrado"));
        Company company = companyRepository.findByIdOptional(dto.companyId)
                .orElseThrow(() -> new BadRequestException("Empresa não encontrada"));

        // Simulação
        LoanSimulationResultDTO result = LoanSimulationUtil.simulate(dto);

        LoanProposal proposal = new LoanProposal();
        proposal.setCustomer(customer);
        proposal.setCompany(company);
        proposal.setRequestedAmount(dto.requestedAmount);
        proposal.setAvailableLimit(dto.availableLimit);
        proposal.setTermInMonths(dto.termInMonths);
        proposal.setConvenioType(dto.convenioType);
        proposal.setStatus(LoanProposalStatus.SIMULATED);
        proposal.setCreatedBy(getCurrentUser());
        proposal.setUpdatedBy(getCurrentUser());

        // Dados calculados
        proposal.setInstallmentValue(result.installmentValue);
        proposal.setTotalPayment(result.totalPayment);
        proposal.setFinancedAmount(result.financedAmount);
        proposal.setGrossAmount(result.grossAmount);
        proposal.setIofTotal(result.iofTotal);
        proposal.setIofAnnualRate(result.iofAnnualRate);
        proposal.setIofAdditionalRate(result.iofAdditionalRate);
        proposal.setProposalDate(result.proposalDate);
        proposal.setFirstInstallmentDate(result.firstInstallmentDate);
        proposal.setLastInstallmentDate(result.lastInstallmentDate);
        proposal.setNumberOfInstallments(result.numberOfInstallments);
        proposal.setTacValue(result.tacValue);
        proposal.setSpread(result.spread);
        proposal.setMonthlyRate(result.monthlyRate);
        proposal.setEffectiveMonthlyRate(result.effectiveMonthlyRate);
        proposal.setEffectiveAnnualRate(result.effectiveAnnualRate);
        proposal.setContractedAnnualRate(result.contractedAnnualRate);
        proposal.setProductType(result.productType);
        proposal.setFund(result.fund);
        proposal.setInstallmentType(result.installmentType);
        proposal.setDisbursementValue(result.disbursementValue);
        proposal.setDisbursementDate(result.disbursementDate);
        proposal.setProcessingCostTotal(result.processingCostTotal);
        proposal.setAmortizationType(result.amortizationType);
        proposal.setInterestComposition(result.interestComposition);
        proposal.setCcb(result.ccb);

        proposalRepository.persist(proposal);
        result.proposal = proposal.getId().toString();

        if (result.installments != null) {
            for (LoanInstallmentDTO i : result.installments) {
                LoanInstallment entity = new LoanInstallment();
                entity.setProposal(proposal);
                entity.setNumber(i.number);
                entity.setValue(i.value);
                entity.setDueDate(i.dueDate);
                entity.setIof(i.iof);
                entity.setBalance(i.balance);
                entity.setInterest(i.interest);
                entity.setPrincipal(i.principal);
                entity.setAdditionalValue(i.additionalValue);
                entity.setPresentValue(i.presentValue);
                entity.setStatus(i.status);
                entity.setPeriod(i.period);
                entity.setDaysElapsed(i.daysElapsed);
                entity.setDaysLate(i.daysLate);
                entity.setPaidValue(i.paidValue);
                installmentRepository.persist(entity);
            }
        }

        return result;
    }

    @Transactional
    public LoanProposalResponseDTO updateProposal(Long id, LoanProposalRequestDTO dto) {
        validateDTO(dto);

        LoanProposal proposal = proposalRepository.findByIdOptional(id)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));
        proposal.getInstallments().size(); // forçar inicialização do Hibernate

        Customer customer = customerRepository.findByIdOptional(dto.customerId)
                .orElseThrow(() -> new BadRequestException("Cliente não encontrado"));

        Company company = companyRepository.findByIdOptional(dto.companyId)
                .orElseThrow(() -> new BadRequestException("Empresa não encontrada"));

        // Atualiza dados básicos da proposta
        proposal.setCustomer(customer);
        proposal.setCompany(company);
        proposal.setRequestedAmount(dto.requestedAmount);
        proposal.setAvailableLimit(dto.availableLimit);
        proposal.setTermInMonths(dto.termInMonths);
        proposal.setConvenioType(dto.convenioType);
        proposal.setStatus(LoanProposalStatus.SIMULATED);
        proposal.setUpdatedBy(getCurrentUser());
        proposal.setUpdatedAt(LocalDateTime.now());

        // Executa simulação
        LoanSimulationResultDTO result = LoanSimulationUtil.simulate(dto);

        // Aplica os dados da simulação na entidade
        applySimulationResult(proposal, result);

        // Limpa parcelas anteriores
        proposal.getInstallments().clear();
        installmentRepository.delete("proposal.id", proposal.getId());

        // Adiciona novas parcelas
        if (result.installments != null) {
            for (LoanInstallmentDTO i : result.installments) {
                LoanInstallment entity = new LoanInstallment();
                entity.setProposal(proposal);
                entity.setNumber(i.number);
                entity.setValue(i.value);
                entity.setDueDate(i.dueDate);
                entity.setIof(i.iof);
                entity.setBalance(i.balance);
                entity.setInterest(i.interest);
                entity.setPrincipal(i.principal);
                entity.setAdditionalValue(i.additionalValue);
                entity.setPresentValue(i.presentValue);
                entity.setStatus(i.status);
                entity.setPeriod(i.period);
                entity.setDaysElapsed(i.daysElapsed);
                entity.setDaysLate(i.daysLate);
                entity.setPaidValue(i.paidValue);
                proposal.getInstallments().add(entity);
            }
        }

        return toResponse(proposal, result);
    }

    private void applySimulationResult(LoanProposal proposal, LoanSimulationResultDTO result) {
        proposal.setInstallmentValue(result.installmentValue);
        proposal.setTotalPayment(result.totalPayment);
        proposal.setFinancedAmount(result.financedAmount);
        proposal.setMonthlyRate(result.monthlyRate);
        proposal.setFirstInstallmentDate(result.firstInstallmentDate);
        proposal.setLastInstallmentDate(result.lastInstallmentDate);
        proposal.setNumberOfInstallments(result.installments != null ? result.installments.size() : 0);
        proposal.setGrossAmount(result.grossAmount);
        proposal.setIofTotal(result.iofTotal);
        proposal.setIofAnnualRate(result.iofAnnualRate);
        proposal.setIofAdditionalRate(result.iofAdditionalRate);
        proposal.setProposalDate(result.proposalDate);
        proposal.setTacValue(result.tacValue);
        proposal.setSpread(result.spread);
        proposal.setEffectiveMonthlyRate(result.effectiveMonthlyRate);
        proposal.setEffectiveAnnualRate(result.effectiveAnnualRate);
        proposal.setContractedAnnualRate(result.contractedAnnualRate);
        proposal.setProductType(result.productType);
        proposal.setFund(result.fund);
        proposal.setInstallmentType(result.installmentType);
        proposal.setDisbursementValue(result.disbursementValue);
        proposal.setDisbursementDate(result.disbursementDate);
        proposal.setProcessingCostTotal(result.processingCostTotal);
        proposal.setAmortizationType(result.amortizationType);
        proposal.setInterestComposition(result.interestComposition);
    }

    public LoanProposalResponseDTO toResponse(LoanProposal proposal, LoanSimulationResultDTO simulation) {
        LoanProposalResponseDTO dto = new LoanProposalResponseDTO();

        dto.id = proposal.getId();
        dto.customerId = proposal.getCustomer().getId();
        dto.companyId = proposal.getCompany().getId();
        dto.customerName = proposal.getCustomer().getName();
        dto.companyName = proposal.getCompany().getName();
        dto.requestedAmount = proposal.getRequestedAmount();
        dto.availableLimit = proposal.getAvailableLimit();
        dto.termInMonths = proposal.getTermInMonths();
        dto.convenioType = proposal.getConvenioType();
        dto.status = proposal.getStatus();
        dto.createdAt = proposal.getCreatedAt();
        dto.updatedAt = proposal.getUpdatedAt();

        // Dados da simulação
        dto.installmentValue = simulation.installmentValue;
        dto.totalPayment = simulation.totalPayment;
        dto.financedAmount = simulation.financedAmount;
        dto.grossAmount = simulation.grossAmount;
        dto.iofTotal = simulation.iofTotal;
        dto.iofAnnualRate = simulation.iofAnnualRate;
        dto.iofAdditionalRate = simulation.iofAdditionalRate;
        dto.proposalDate = simulation.proposalDate;
        dto.firstInstallmentDate = simulation.firstInstallmentDate != null ? simulation.firstInstallmentDate.toString()
                : null;
        dto.lastInstallmentDate = simulation.lastInstallmentDate != null ? simulation.lastInstallmentDate.toString()
                : null;
        dto.numberOfInstallments = simulation.numberOfInstallments;
        dto.tacValue = simulation.tacValue;
        dto.spread = simulation.spread;
        dto.monthlyRate = simulation.monthlyRate;
        dto.effectiveMonthlyRate = simulation.effectiveMonthlyRate;
        dto.effectiveAnnualRate = simulation.effectiveAnnualRate;
        dto.contractedAnnualRate = simulation.contractedAnnualRate;
        dto.productType = simulation.productType;
        dto.fund = simulation.fund;
        dto.installmentType = simulation.installmentType;
        dto.disbursementValue = simulation.disbursementValue;
        dto.disbursementDate = simulation.disbursementDate;
        dto.processingCostTotal = simulation.processingCostTotal;
        dto.amortizationType = simulation.amortizationType;
        dto.interestComposition = simulation.interestComposition;
        dto.ccb = simulation.ccb;

        dto.installments = simulation.installments;

        return dto;
    }

    @Transactional
    public void payInstallment(Long proposalId, Integer installmentNumber, Long bankAccountId) {
        LoanProposal proposal = proposalRepository.findByIdOptional(proposalId)
                .orElseThrow(() -> new BadRequestException("Proposta não encontrada"));

        LoanInstallment installment = installmentRepository
                .find("proposal.id = ?1 and number = ?2", proposalId, installmentNumber)
                .firstResultOptional()
                .orElseThrow(() -> new BadRequestException("Parcela não encontrada para essa proposta"));

        if (installment.isPaid()) {
            throw new BadRequestException("Parcela já foi paga");
        }

        BankAccount account = bankAccountRepository.findByIdOptional(bankAccountId)
                .orElseThrow(() -> new BadRequestException("Conta bancária não encontrada"));

        if (!account.getCustomer().getId().equals(proposal.getCustomer().getId())) {
            throw new BadRequestException("Conta não pertence ao cliente da proposta");
        }

        if (account.getBalance().compareTo(installment.getValue()) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance().subtract(installment.getValue()));
        bankAccountRepository.persist(account);

        installment.setPaid(true);
        installment.setPaidValue(installment.getValue());
        installmentRepository.persist(installment);

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setToAccount(null); // ou conta da empresa
        tx.setAmount(installment.getValue());
        tx.setType(TransactionType.LOAN_INSTALLMENT_PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Pagamento da parcela #" + installment.getNumber() + " da proposta #" + proposalId);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.persist(tx);
    }

    public BigDecimal getAvailableMargin(Long customerId) {
        // Busca o cliente
        Customer customer = customerRepository.findByIdOptional(customerId)
                .orElseThrow(() -> new BadRequestException("Cliente não encontrado"));

        // Calcula a margem máxima permitida (40% do salário)
        BigDecimal salary = customer.getSalary() != null ? customer.getSalary() : BigDecimal.ZERO;
        BigDecimal maxMargin = salary.multiply(new BigDecimal("0.40"));

        // Busca todas as parcelas não pagas de propostas ativas (excluindo CANCELED)
        List<LoanInstallment> unpaidInstallments = installmentRepository.find(
                "proposal.customer.id = ?1 AND paid = false AND proposal.status != ?2 ORDER BY number ASC",
                customerId, LoanProposalStatus.CANCELED).list();

        // Armazena a menor (mais próxima) parcela de cada proposta ativa
        Map<Long, LoanInstallment> firstInstallmentPerProposal = new HashMap<>();

        for (LoanInstallment installment : unpaidInstallments) {
            Long proposalId = installment.getProposal().getId();
            if (!firstInstallmentPerProposal.containsKey(proposalId)) {
                firstInstallmentPerProposal.put(proposalId, installment);
            }
        }

        // Soma o valor da menor parcela de cada proposta ativa
        BigDecimal committedMonthly = firstInstallmentPerProposal.values().stream()
                .map(LoanInstallment::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Retorna a margem disponível
        return maxMargin.subtract(committedMonthly).max(BigDecimal.ZERO);
    }

    public void validateAvailableMargin(Long customerId, BigDecimal newInstallmentValue) {
        BigDecimal available = getAvailableMargin(customerId);

        if (newInstallmentValue.compareTo(available) > 0) {
            throw new BadRequestException("Parcela excede a margem consignável disponível. Restante: R$ " + available);
        }
    }

}