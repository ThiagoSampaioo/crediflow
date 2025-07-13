package com.crediflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LoanSimulationResultDTO {

    public BigDecimal installmentValue; // Valor da Parcela
    public BigDecimal totalPayment;     // Valor do Pagamento
    public BigDecimal financedAmount;   // Valor Financiado
    public BigDecimal grossAmount;      // Valor Bruto Financiado
    public BigDecimal iofTotal;         // IOF Total
    public BigDecimal iofAnnualRate;
    public BigDecimal iofAdditionalRate;

    public LocalDate proposalDate;
    public LocalDate firstInstallmentDate;
    public LocalDate lastInstallmentDate;

    public Integer numberOfInstallments;
    public BigDecimal tacValue;
    public BigDecimal spread;
    public BigDecimal monthlyRate;              // Taxa Mensal
    public BigDecimal effectiveMonthlyRate;     // Taxa Efetiva Mensal
    public BigDecimal effectiveAnnualRate;      // Taxa Efetiva Anual
    public BigDecimal contractedAnnualRate;     // Taxa Contratada Anual

    public String productType;
    public String fund;
    public String installmentType;
    public BigDecimal disbursementValue;
    public LocalDate disbursementDate;
    public BigDecimal processingCostTotal;
    public String amortizationType;
    public String interestComposition;
    public String ccb;
    public String proposal;

    public List<LoanInstallmentDTO> installments;
}
