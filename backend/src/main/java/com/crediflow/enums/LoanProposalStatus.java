package com.crediflow.enums;

public enum LoanProposalStatus {
    SIMULATED,        // Simulação inicial
    PENDING_SIGNATURE, // CCB aguardando assinatura
    SIGNED,           // CCB assinada
    UNDER_REVIEW,     // Em análise (antes do pagamento)
    APPROVED,         // Aprovada para pagamento
    REJECTED,         // Reprovada
    PAID,             // Pagamento realizado
    CANCELED          // Proposta cancelada
}
