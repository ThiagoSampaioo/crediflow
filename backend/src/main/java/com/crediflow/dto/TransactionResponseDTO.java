package com.crediflow.dto;

import com.crediflow.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {

    public Long id;
    public Long fromAccountId;
    public Long toAccountId;
    public BigDecimal amount;
    public String type;
    public String status;
    public String description;
    public LocalDateTime createdAt;

    /**
     * Converte uma entidade Transaction para um DTO simplificado
     */
    public static TransactionResponseDTO fromEntity(final Transaction tx) {
        TransactionResponseDTO dto = new TransactionResponseDTO();

        dto.id = tx.getId();
        dto.fromAccountId = tx.getFromAccount() != null ? tx.getFromAccount().getId() : null;
        dto.toAccountId = tx.getToAccount() != null ? tx.getToAccount().getId() : null;
        dto.amount = tx.getAmount();
        dto.type = tx.getType() != null ? tx.getType().name() : null;
        dto.status = tx.getStatus() != null ? tx.getStatus().name() : null;
        dto.description = tx.getDescription();
        dto.createdAt = tx.getCreatedAt();

        return dto;
    }
}
