package com.crediflow.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanProposalRequestDTO {

    @NotNull
    public Long customerId;

    @NotNull
    public Long companyId;

    @NotNull
    @DecimalMin("0.0")
    public BigDecimal requestedAmount;

    @NotNull
    @DecimalMin("0.0")
    public BigDecimal availableLimit;

    @NotNull
    @Min(1)
    @Max(96)
    public Integer termInMonths;

    @NotBlank
    public String convenioType;

    @NotNull
    @DecimalMin("0.0")
    public BigDecimal monthlyInterestRate;

    @NotNull
    public LocalDate firstInstallmentDate;

    @NotBlank
    public String modoSimulacao;

    // Campos adicionais para simulação
    public BigDecimal grossAmount;
    public BigDecimal iofTotal;
    public BigDecimal iofAnnualRate;
    public BigDecimal iofAdditionalRate;
    public LocalDate proposalDate;
    public BigDecimal tacValue;
    public BigDecimal spread;
    public BigDecimal effectiveMonthlyRate;
    public BigDecimal effectiveAnnualRate;
    public BigDecimal contractedAnnualRate;
    public String productType;
    public String fund;
    public String installmentType;
    public BigDecimal disbursementValue;
    public LocalDate disbursementDate;
    public BigDecimal processingCostTotal;
    public String amortizationType;
    public String interestComposition;
    public String ccb;
}
