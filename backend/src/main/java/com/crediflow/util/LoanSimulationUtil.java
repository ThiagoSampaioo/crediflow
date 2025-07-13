package com.crediflow.util;

import com.crediflow.dto.LoanInstallmentDTO;
import com.crediflow.dto.LoanProposalRequestDTO;
import com.crediflow.dto.LoanSimulationResultDTO;
import com.crediflow.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanSimulationUtil {

    public static LoanSimulationResultDTO simulate(LoanProposalRequestDTO dto) {
        int months = dto.termInMonths;
        BigDecimal rate = dto.monthlyInterestRate;
        LocalDate firstDueDate = dto.firstInstallmentDate;

        BigDecimal amountFinanced;
        BigDecimal monthlyInstallment;

        if ("VALOR_FINANCIADO".equalsIgnoreCase(dto.modoSimulacao)) {
            amountFinanced = dto.requestedAmount;
            monthlyInstallment = calculateInstallment(amountFinanced, rate, months);
        } else if ("VALOR_PARCELA".equalsIgnoreCase(dto.modoSimulacao)) {
            monthlyInstallment = dto.requestedAmount;
            amountFinanced = calculateAmountFinanced(monthlyInstallment, rate, months);
        } else {
            //mandando mensagem sem quebrar o fluxo
            throw new BadRequestException("Modo de simulação inválido. Use 'VALOR_FINANCIADO' ou 'VALOR_PARCELA'.");
        }

        BigDecimal totalPayment = monthlyInstallment.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = totalPayment.subtract(amountFinanced).setScale(2, RoundingMode.HALF_UP);

        List<LoanInstallmentDTO> schedule = new ArrayList<>();
        BigDecimal remaining = amountFinanced;

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = firstDueDate.plusMonths(i - 1);
            BigDecimal interest = remaining.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = monthlyInstallment.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            remaining = remaining.subtract(principal).setScale(2, RoundingMode.HALF_UP);

            LoanInstallmentDTO installment = new LoanInstallmentDTO();
            installment.number = i;
            installment.dueDate = dueDate;
            installment.value = monthlyInstallment;
            installment.interest = interest;
            installment.principal = principal;
            installment.balance = remaining.max(BigDecimal.ZERO);
            schedule.add(installment);
        }

        LoanSimulationResultDTO result = new LoanSimulationResultDTO();
        result.financedAmount = amountFinanced.setScale(2, RoundingMode.HALF_UP);
        result.installmentValue = monthlyInstallment.setScale(2, RoundingMode.HALF_UP);
        result.totalPayment = totalPayment;
        result.iofTotal = BigDecimal.ZERO;
        result.iofAnnualRate = BigDecimal.ZERO;
        result.iofAdditionalRate = BigDecimal.ZERO;

        result.monthlyRate = rate;
        result.effectiveMonthlyRate = rate; // simplificado
        result.effectiveAnnualRate = rate.multiply(BigDecimal.valueOf(12)).setScale(4, RoundingMode.HALF_UP);
        result.contractedAnnualRate = rate.multiply(BigDecimal.valueOf(12)).setScale(4, RoundingMode.HALF_UP);

        result.firstInstallmentDate = firstDueDate;
        result.lastInstallmentDate = firstDueDate.plusMonths(months - 1);
        result.numberOfInstallments = months;

        result.tacValue = BigDecimal.ZERO;
        result.spread = BigDecimal.ZERO;

        result.productType = "CONSIGNADO";
        result.fund = "Fundo Genérico";
        result.installmentType = "PRICE";
        result.amortizationType = "PRICE";
        result.interestComposition = "COMPOSTO";
        result.ccb = "N/A";
        result.processingCostTotal = BigDecimal.ZERO;

        result.disbursementValue = amountFinanced.setScale(2, RoundingMode.HALF_UP);
        result.disbursementDate = LocalDate.now();

        result.installments = schedule;

        return result;
    }

    private static BigDecimal calculateInstallment(BigDecimal principal, BigDecimal rate, int months) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusI = BigDecimal.ONE.add(rate);
        BigDecimal numerator = rate.multiply(onePlusI.pow(months));
        BigDecimal denominator = onePlusI.pow(months).subtract(BigDecimal.ONE);
        return principal.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateAmountFinanced(BigDecimal installment, BigDecimal rate, int months) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return installment.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusI = BigDecimal.ONE.add(rate);
        BigDecimal numerator = onePlusI.pow(months).subtract(BigDecimal.ONE);
        BigDecimal denominator = rate.multiply(onePlusI.pow(months));
        return installment.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
