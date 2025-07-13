package com.crediflow.dto;

import com.crediflow.enums.LoanProposalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LoanProposalResponseDTO {
    public Long id;
    public Long customerId;
    public Long companyId;
    public String customerName;
    public String companyName;
    public BigDecimal requestedAmount;
    public BigDecimal availableLimit;
    public Integer termInMonths;
    public String convenioType;
    public LoanProposalStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public BigDecimal installmentValue;
    public BigDecimal totalPayment;
    public BigDecimal financedAmount;
    public BigDecimal monthlyRate;
    public String firstInstallmentDate;
    public String lastInstallmentDate;
    public Integer numberOfInstallments;

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

    public List<LoanInstallmentDTO> installments;
}
