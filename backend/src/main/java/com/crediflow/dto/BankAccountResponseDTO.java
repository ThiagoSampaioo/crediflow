package com.crediflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccountResponseDTO {
    public Long id;
    public String accountNumber;
    public String agencyNumber;
    public BigDecimal balance;
    public Long customerId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String pixKey; // Chave Pix associada à conta bancária
    public String pixKeyType; // Tipo da chave Pix (CPF, Email, Telefone, Aleatória, etc.)
  
}
