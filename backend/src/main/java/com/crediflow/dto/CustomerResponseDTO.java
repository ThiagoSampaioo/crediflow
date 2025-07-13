package com.crediflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerResponseDTO {

    public Long id;
    public String keycloakId;
    public Boolean enabled;
    public String name;
    public String cpf;
    public String email;
    public String phone;
    public LocalDate birthDate;
    public Long companyId;
    public String companyName;
    public String occupation;

    public String street;
    public Number streetNumber;
    public String city;
    public String neighborhood;
    public String state;
    public String zipCode;
    public String country;

    public String createdBy;
    public String updatedBy;
    public LocalDate createdAt;
    public LocalDate updatedAt;


    public String virtualAccountNumber;
    public String virtualAgencyNumber;
    public BigDecimal virtualBalance;

    public BigDecimal salary = BigDecimal.ZERO; // Salário do cliente, se aplicável
    
}
