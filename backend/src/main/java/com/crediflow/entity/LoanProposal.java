package com.crediflow.entity;

import com.crediflow.enums.LoanProposalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "loan_proposals")
public class LoanProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal requestedAmount;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal availableLimit;

    @NotNull
    private Integer termInMonths;

    @NotBlank
    private String convenioType;

    @Enumerated(EnumType.STRING)
    private LoanProposalStatus status = LoanProposalStatus.SIMULATED;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String createdBy;
    private String updatedBy;

    private BigDecimal installmentValue;
    private BigDecimal totalPayment;
    private BigDecimal financedAmount;
    private BigDecimal monthlyRate;
    private LocalDate firstInstallmentDate;
    private LocalDate lastInstallmentDate;
    private Integer numberOfInstallments;
    private BigDecimal grossAmount;
    private BigDecimal iofTotal;
    private BigDecimal iofAnnualRate;
    private BigDecimal iofAdditionalRate;
    private LocalDate proposalDate;
    private BigDecimal tacValue;
    private BigDecimal spread;
    private BigDecimal effectiveMonthlyRate;
    private BigDecimal effectiveAnnualRate;
    private BigDecimal contractedAnnualRate;
    private String productType;
    private String fund;
    private String installmentType;
    private BigDecimal disbursementValue;
    private LocalDate disbursementDate;
    private BigDecimal processingCostTotal;
    private String amortizationType;
    private String interestComposition;
    private String ccb;
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<LoanInstallment> installments;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAvailableLimit() {
        return availableLimit;
    }

    public void setAvailableLimit(BigDecimal availableLimit) {
        this.availableLimit = availableLimit;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }

    public String getConvenioType() {
        return convenioType;
    }

    public void setConvenioType(String convenioType) {
        this.convenioType = convenioType;
    }

    public LoanProposalStatus getStatus() {
        return status;
    }

    public void setStatus(LoanProposalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public BigDecimal getInstallmentValue() {
        return installmentValue;
    }

    public void setInstallmentValue(BigDecimal installmentValue) {
        this.installmentValue = installmentValue;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public BigDecimal getFinancedAmount() {
        return financedAmount;
    }

    public void setFinancedAmount(BigDecimal financedAmount) {
        this.financedAmount = financedAmount;
    }

    public BigDecimal getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(BigDecimal monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    public LocalDate getFirstInstallmentDate() {
        return firstInstallmentDate;
    }

    public void setFirstInstallmentDate(LocalDate firstInstallmentDate) {
        this.firstInstallmentDate = firstInstallmentDate;
    }

    public LocalDate getLastInstallmentDate() {
        return lastInstallmentDate;
    }

    public void setLastInstallmentDate(LocalDate lastInstallmentDate) {
        this.lastInstallmentDate = lastInstallmentDate;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public List<LoanInstallment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<LoanInstallment> installments) {
        this.installments = installments;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getIofTotal() {
        return iofTotal;
    }

    public void setIofTotal(BigDecimal iofTotal) {
        this.iofTotal = iofTotal;
    }

    public BigDecimal getIofAnnualRate() {
        return iofAnnualRate;
    }

    public void setIofAnnualRate(BigDecimal iofAnnualRate) {
        this.iofAnnualRate = iofAnnualRate;
    }

    public BigDecimal getIofAdditionalRate() {
        return iofAdditionalRate;
    }

    public void setIofAdditionalRate(BigDecimal iofAdditionalRate) {
        this.iofAdditionalRate = iofAdditionalRate;
    }

    public LocalDate getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(LocalDate proposalDate) {
        this.proposalDate = proposalDate;
    }

    public BigDecimal getTacValue() {
        return tacValue;
    }

    public void setTacValue(BigDecimal tacValue) {
        this.tacValue = tacValue;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public BigDecimal getEffectiveMonthlyRate() {
        return effectiveMonthlyRate;
    }

    public void setEffectiveMonthlyRate(BigDecimal effectiveMonthlyRate) {
        this.effectiveMonthlyRate = effectiveMonthlyRate;
    }

    public BigDecimal getEffectiveAnnualRate() {
        return effectiveAnnualRate;
    }

    public void setEffectiveAnnualRate(BigDecimal effectiveAnnualRate) {
        this.effectiveAnnualRate = effectiveAnnualRate;
    }

    public BigDecimal getContractedAnnualRate() {
        return contractedAnnualRate;
    }

    public void setContractedAnnualRate(BigDecimal contractedAnnualRate) {
        this.contractedAnnualRate = contractedAnnualRate;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getInstallmentType() {
        return installmentType;
    }

    public void setInstallmentType(String installmentType) {
        this.installmentType = installmentType;
    }

    public BigDecimal getDisbursementValue() {
        return disbursementValue;
    }

    public void setDisbursementValue(BigDecimal disbursementValue) {
        this.disbursementValue = disbursementValue;
    }

    public LocalDate getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDate disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public BigDecimal getProcessingCostTotal() {
        return processingCostTotal;
    }

    public void setProcessingCostTotal(BigDecimal processingCostTotal) {
        this.processingCostTotal = processingCostTotal;
    }

    public String getAmortizationType() {
        return amortizationType;
    }

    public void setAmortizationType(String amortizationType) {
        this.amortizationType = amortizationType;
    }

    public String getInterestComposition() {
        return interestComposition;
    }

    public void setInterestComposition(String interestComposition) {
        this.interestComposition = interestComposition;
    }

    public String getCcb() {
        return ccb;
    }

    public void setCcb(String ccb) {
        this.ccb = ccb;
    }
}
