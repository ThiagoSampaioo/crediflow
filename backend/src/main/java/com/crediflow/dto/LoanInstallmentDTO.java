package com.crediflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanInstallmentDTO {
    public Long id;                     // ID da parcela
    public Integer number;                  // Nº da parcela
    public BigDecimal value;               // Valor da parcela
    public LocalDate dueDate;              // Data de vencimento
    public BigDecimal iof;                 // IOF da parcela
    public BigDecimal balance;             // Saldo após a parcela
    public BigDecimal interest;            // Juros
    public BigDecimal principal;           // Amortização
    public BigDecimal additionalValue;     // Valor adicional (se houver)
    public BigDecimal presentValue;        // Valor presente (se calculado)
    public String status;                  // Status da parcela (ex: PENDENTE, PAGO)
    public Integer period;                 // Número do período
    public Integer daysElapsed;            // Dias corridos desde o início
    public Integer daysLate;               // Dias de atraso
    public BigDecimal paidValue;           // Valor pago (se houver)
    public boolean paid;                   // Indica se a parcela foi paga
}
