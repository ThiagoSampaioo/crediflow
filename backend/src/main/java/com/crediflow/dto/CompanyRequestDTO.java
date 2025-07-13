package com.crediflow.dto;

import jakarta.validation.constraints.*;

public class CompanyRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    public String name;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
    public String cnpj;

    @NotBlank(message = "Nome do responsável é obrigatório")
    public String responsible;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    public String email;

    @NotBlank(message = "Telefone é obrigatório")
    public String phone;

    @NotBlank(message = "Tipo de convênio é obrigatório")
    public String type;
}
