package com.crediflow.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    public String name;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$", message = "CPF inválido. Deve estar no formato XXX.XXX.XXX-XX ou apenas números.")
    @Size(min = 11, max = 14, message = "O CPF deve ter entre 11 e 14 caracteres, incluindo pontos e traços.")
    public String cpf;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    public String email;
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefone inválido. Deve conter apenas números e ter entre 10 a 15 dígitos.")
    @NotBlank(message = "O telefone é obrigatório")
    public String phone;

    public LocalDate birthDate;

    @NotNull(message = "O ID da empresa conveniada é obrigatório")
    public Long companyId;
    @NotBlank(message = "Informe a ocupação ou profissão")
    public String occupation;

    @NotBlank(message = "O logradouro é obrigatório")
    public String street;
    @NotNull(message = "O número da residência é obrigatório")
    @Min(value = 1, message = "O número da residência deve ser um valor positivo")
    public Number streetNumber; // Pode ser inteiro ou decimal, dependendo do formato do endereço
    @NotBlank(message = "A cidade é obrigatória")
    public String city;
    @NotBlank(message = "O bairro é obrigatório")
    public String neighborhood;
    @NotBlank(message = "O estado é obrigatório")
    public String state;
    @NotBlank(message = "O CEP é obrigatório")
    public String zipCode;
    @NotBlank(message = "O país é obrigatório")
    public String country;
    public String keycloakId; // ID do usuário no Keycloak, se aplicável
    public String createdBy; // Usuário que criou o registro
    public String updatedBy; // Usuário que atualizou o registro
    public LocalDate createdAt; // Data de criação do registro
    public LocalDate updatedAt; // Data da última atualização do registro
    @DecimalMin(value = "0.0", inclusive = false, message = "O salário deve ser um valor positivo")
    public BigDecimal salary = BigDecimal.ZERO; // Salário do cliente, se aplicável


}
