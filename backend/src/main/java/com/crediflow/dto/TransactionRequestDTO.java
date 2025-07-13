package com.crediflow.dto;


public class TransactionRequestDTO {
    public Long fromAccountId;       // Para transferências e Pix
    public Long toAccountId;         // Usado quando for direto por ID
    public String agencyNumber;      // Para transferir/depositar via agência + conta
    public String accountNumber;
    public String pixKey;            // Para Pix
    public String amount;
    public String type;              // TRANSFER, PIX, DEPOSIT etc.
    public String description;
}
