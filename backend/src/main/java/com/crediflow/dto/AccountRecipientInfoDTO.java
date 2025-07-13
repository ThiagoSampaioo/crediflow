package com.crediflow.dto;

public class AccountRecipientInfoDTO {
    public String name;
    public String agencyNumber;
    public String accountNumber;
    public String pixKey;
    public String pixKeyType;

    public AccountRecipientInfoDTO(String name, String agencyNumber, String accountNumber, String pixKey, String pixKeyType) {
        this.name = name;
        this.agencyNumber = agencyNumber;
        this.accountNumber = accountNumber;
        this.pixKey = pixKey;
        this.pixKeyType = pixKeyType;
    }
}
