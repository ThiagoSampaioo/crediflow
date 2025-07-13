package com.crediflow.service;

import com.crediflow.config.KeycloakConfig;
import com.crediflow.dto.CompanyRequestDTO;
import com.crediflow.dto.CompanyResponseDTO;
import com.crediflow.dto.CustomerResponseDTO;
import com.crediflow.dto.PagedResult;
import com.crediflow.entity.Company;
import com.crediflow.entity.Customer;
import com.crediflow.exception.BadRequestException;
import com.crediflow.exception.ConflictException;
import com.crediflow.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CompanyService {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    KeycloakAdminService keycloakService;

    @Inject
    ObjectMapper mapper;

    @Inject
    KeycloakConfig keycloakConfig;

    @Inject
    SecurityIdentity securityIdentity;

    @Transactional
    public Company create(Company company) {
        Optional<Company> existingCompany = companyRepository.find("cnpj", company.getCnpj()).firstResultOptional();
        if (existingCompany.isPresent()) {
            throw new ConflictException("Já existe uma empresa com este CNPJ.");
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", company.getEmail());
        userData.put("email", company.getEmail());
        userData.put("firstName", company.getName());
        userData.put("lastName", company.getResponsibleName());
        userData.put("password", "123456");

        try {
            Optional<Map<String, Object>> existingUser = keycloakService.getUserByUsername(company.getEmail());

            String keycloakId;
            if (existingUser.isPresent()) {
                keycloakId = (String) existingUser.get().get("id");
            } else {
                keycloakId = keycloakService.createUserAndReturnId(userData, "company");
            }

            company.setKeycloakId(keycloakId);
        } catch (Exception e) {
            throw new BadRequestException("Erro ao criar/verificar usuário no Keycloak: " + e.getMessage());
        }

        company.setCreatedBy(getCurrentUsername());
        companyRepository.persist(company);
        return company;
    }

    public List<Company> findAll() {
        return companyRepository.listAll();
    }

    public PagedResult<CompanyResponseDTO> findAllPaged(int page, int size) {
        PanacheQuery<Company> query = companyRepository.findAll();
        long total = query.count();

        List<CompanyResponseDTO> items = query.page(Page.of(page, size))
            .list()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return new PagedResult<>(items, total, page, size);
    }

    public PagedResult<CompanyResponseDTO> search(String query, int page, int size) {
        PanacheQuery<Company> results = companyRepository.searchPaged(query, page, size);
        long total = results.count();

        List<CompanyResponseDTO> items = results.list()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return new PagedResult<>(items, total, page, size);
    }




    public Optional<Company> getById(Long id) {
        return companyRepository.findByIdOptional(id);
    }

    public List<Company> searchByName(String name) {
        return companyRepository.list("LOWER(name) LIKE LOWER(?1)", "%" + name + "%");
    }

   @Transactional
public boolean delete(Long id) {
    Optional<Company> optional = companyRepository.findByIdOptional(id);

    if (optional.isEmpty()) {
        return false;
    }

    Company company = optional.get();

    // 1. Tenta deletar do Keycloak
    try {
        if (company.getKeycloakId() != null) {
            keycloakService.deleteUser(company.getKeycloakId());
        }
    } catch (Exception e) {
        throw new BadRequestException("Erro ao deletar usuário no Keycloak: " + e.getMessage());
    }

    // 2. Deleta do banco
    return companyRepository.deleteById(id);
}


    @Transactional
    public void updateEntity(Company company, CompanyRequestDTO dto) {
        company.setName(dto.name);
        company.setCnpj(dto.cnpj);
        company.setResponsibleName(dto.responsible);
        company.setEmail(dto.email);
        company.setConvenioType(dto.type);
        company.setPhone(dto.phone);
        company.setUpdatedBy(getCurrentUsername());
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", dto.email);
            userData.put("firstName", dto.name);
            userData.put("lastName", dto.responsible);
            userData.put("email", dto.email);

            keycloakService.updateUser(company.getKeycloakId(), userData);
        } catch (Exception e) {
            throw new BadRequestException("Erro ao atualizar usuário no Keycloak: " + e.getMessage());
        }
    }

    public Company fromDTO(CompanyRequestDTO dto) {
        Company company = new Company();
        company.setName(dto.name);
        company.setCnpj(dto.cnpj);
        company.setResponsibleName(dto.responsible);
        company.setEmail(dto.email);
        company.setConvenioType(dto.type);
        company.setPhone(dto.phone);
        return company;
    }

    public CompanyResponseDTO toDTO(Company company) {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.id = company.getId();
        dto.keycloakId = company.getKeycloakId();
        dto.name = company.getName();
        dto.cnpj = company.getCnpj();
        dto.responsible = company.getResponsibleName();
        dto.email = company.getEmail();
        dto.type = company.getConvenioType();
        dto.phone = company.getPhone();
        return dto;
    }

    private String getCurrentUsername() {
        return securityIdentity != null && !securityIdentity.isAnonymous()
                ? securityIdentity.getPrincipal().getName()
                : "system";
    }

    // buscando empresa por ID do Keycloak
    public Optional<Company> findByKeycloakId(String keycloakId) {
        return companyRepository.find("keycloakId", keycloakId).firstResultOptional();
    }
}
