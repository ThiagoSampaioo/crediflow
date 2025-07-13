package com.crediflow.service;

import com.crediflow.config.KeycloakConfig;
import com.crediflow.dto.CustomerRequestDTO;
import com.crediflow.dto.CustomerResponseDTO;
import com.crediflow.dto.PagedResult;
import com.crediflow.entity.Company;
import com.crediflow.entity.Customer;
import com.crediflow.exception.BadRequestException;
import com.crediflow.exception.ConflictException;
import com.crediflow.repository.CompanyRepository;
import com.crediflow.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    KeycloakAdminService keycloakService;

    @Inject
    ObjectMapper mapper;

    @Inject
    SecurityIdentity identity;

    @Inject
    KeycloakConfig config;

    public String getCurrentUsername() {
        return identity != null && !identity.isAnonymous()
                ? identity.getPrincipal().getName()
                : "system";
    }

    @Transactional
    public Customer create(Customer customer) {
        // Verifica se já existe CPF ou email
        if (customerRepository.find("cpf", customer.getCpf()).firstResultOptional().isPresent()) {
            throw new ConflictException("Já existe um cliente com este CPF.");
        }
        if (customerRepository.find("email", customer.getEmail()).firstResultOptional().isPresent()) {
            throw new ConflictException("Já existe um cliente com este e-mail.");
        }

        // Cria ou vincula Keycloak user
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", customer.getEmail());
        userData.put("email", customer.getEmail());
        userData.put("firstName", customer.getName());
        userData.put("lastName", customer.getCpf());
        userData.put("password", "123456");

        try {
            Optional<Map<String, Object>> existingUser = keycloakService.getUserByUsername(customer.getEmail());
            String keycloakId;

            if (existingUser.isPresent()) {
                keycloakId = (String) existingUser.get().get("id");
            } else {
                keycloakId = keycloakService.createUserAndReturnId(userData, "client");
            }

            customer.setKeycloakId(keycloakId);
        } catch (Exception e) {
            throw new BadRequestException("Erro ao criar/verificar usuário no Keycloak: " + e.getMessage());
        }

        customer.setCreatedBy(getCurrentUsername());
        customerRepository.persist(customer);
        return customer;
    }

    public Optional<Customer> getById(Long id) {
        return customerRepository.findByIdOptional(id);
    }

    public List<Customer> findAll() {
        return customerRepository.listAll();
    }

    public List<Customer> findByCompanyId(Long companyId) {
        return customerRepository.list("company.id = ?1", companyId);
    }

    public PagedResult<CustomerResponseDTO> findAllPaged(int page, int size) {
        PanacheQuery<Customer> query = customerRepository.findAll();
        long total = query.count();

        List<CustomerResponseDTO> items = query
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResult<>(items, total, page, size);
    }

    public PagedResult<CustomerResponseDTO> search(String query, int page, int size) {
        PanacheQuery<Customer> results = customerRepository.searchPaged(query, page, size);
        long total = results.count();

        List<CustomerResponseDTO> items = results.list()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResult<>(items, total, page, size);
    }

    @Transactional
    public void updateEntity(Customer customer, CustomerRequestDTO dto) {
        try {

            // Verifica se já existe CPF ou email
            if (!customer.getCpf().equals(dto.cpf)
                    && customerRepository.find("cpf", dto.cpf).firstResultOptional().isPresent()) {
                throw new ConflictException("Já existe um cliente com este CPF.");
            }
            if (!customer.getEmail().equals(dto.email)
                    && customerRepository.find("email", dto.email).firstResultOptional().isPresent()) {
                throw new ConflictException("Já existe um cliente com este e-mail.");
            }
            customer.setName(dto.name);
            customer.setCpf(dto.cpf);
            customer.setEmail(dto.email);
            customer.setPhone(dto.phone);
            customer.setBirthDate(dto.birthDate);
            customer.setUpdatedBy(getCurrentUsername());
            customer.setOccupation(dto.occupation);
            customer.setSalary(dto.salary);
            customer.setStreet(dto.street);
            customer.setStreetNumber(dto.streetNumber);
            customer.setCity(dto.city);
            customer.setNeighborhood(dto.neighborhood);
            customer.setState(dto.state);
            customer.setZipCode(dto.zipCode);
            customer.setCountry(dto.country);
            customer.setUpdatedAt(LocalDate.now());
            // Vincula a empresa

            Company company = companyRepository.findById(dto.companyId);
            if (company == null) {
                throw new BadRequestException("Empresa vinculada não encontrada.");
            }
            customer.setCompany(company);

            // Atualiza também no Keycloak
            try {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", dto.email);
                userData.put("email", dto.email);
                userData.put("firstName", dto.name);
                userData.put("lastName", dto.cpf);
                keycloakService.updateUser(customer.getKeycloakId(), userData);
            } catch (Exception e) {
                throw new BadRequestException("Erro ao atualizar usuário no Keycloak: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new BadRequestException("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Customer> opt = customerRepository.findByIdOptional(id);
        if (opt.isEmpty())
            return false;

        Customer customer = opt.get();

        try {
            keycloakService.deleteUser(customer.getKeycloakId());
        } catch (Exception e) {
            throw new BadRequestException("Erro ao deletar usuário no Keycloak: " + e.getMessage());
        }

        return customerRepository.deleteById(id);
    }

    public Customer fromDTO(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.name);
        customer.setCpf(dto.cpf);
        customer.setEmail(dto.email);
        customer.setPhone(dto.phone);
        customer.setBirthDate(dto.birthDate);
        customer.setOccupation(dto.occupation);
        customer.setSalary(dto.salary);
        customer.setStreet(dto.street);
        customer.setStreetNumber(dto.streetNumber);
        customer.setCity(dto.city);
        customer.setNeighborhood(dto.neighborhood);
        customer.setState(dto.state);
        customer.setZipCode(dto.zipCode);
        customer.setCountry(dto.country);
        customer.setCreatedAt(LocalDate.now());

        Company company = companyRepository.findById(dto.companyId);
        if (company == null) {
            throw new BadRequestException("Empresa vinculada não encontrada.");
        }

        customer.setCompany(company);
        return customer;
    }

    public CustomerResponseDTO toDTO(Customer customer) {
        boolean enabled = true;
        if (customer.getKeycloakId() != null) {
            try {
                enabled = keycloakService.isUserEnabled(customer.getKeycloakId());
            } catch (Exception e) {
                // logar erro se necessário
            }
        }
        return toDTO(customer, enabled);
    }

    public CustomerResponseDTO toDTO(Customer customer, boolean enabled) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.id = customer.getId();
        dto.keycloakId = customer.getKeycloakId();
        dto.name = customer.getName();
        dto.cpf = customer.getCpf();
        dto.email = customer.getEmail();
        dto.phone = customer.getPhone();
        dto.birthDate = customer.getBirthDate();
        dto.companyId = customer.getCompany().getId();
        dto.companyName = customer.getCompany().getName();
        dto.occupation = customer.getOccupation();
        dto.salary = customer.getSalary();
        dto.street = customer.getStreet();
        dto.streetNumber = customer.getStreetNumber();
        dto.city = customer.getCity();
        dto.neighborhood = customer.getNeighborhood();
        dto.state = customer.getState();
        dto.zipCode = customer.getZipCode();
        dto.country = customer.getCountry();
        dto.createdBy = customer.getCreatedBy();
        dto.updatedBy = customer.getUpdatedBy();
        dto.createdAt = customer.getCreatedAt();
        dto.updatedAt = customer.getUpdatedAt();
        dto.enabled = enabled;
        if (customer.getVirtualAccount() != null) {
            dto.virtualAccountNumber = customer.getVirtualAccount().getAccountNumber();
            dto.virtualAgencyNumber = customer.getVirtualAccount().getAgencyNumber();
            dto.virtualBalance = customer.getVirtualAccount().getBalance();
        } else {
            dto.virtualAccountNumber = null;
            dto.virtualAgencyNumber = null;
            dto.virtualBalance = null;
        }
        return dto;
    }

    public PagedResult<CustomerResponseDTO> findByCompanyKeycloakId(String keycloakId, int page, int size) {
        if (keycloakId == null || keycloakId.isEmpty()) {
            throw new BadRequestException("O Keycloak ID da empresa não pode ser nulo ou vazio.");
        }

        Company company = companyRepository.find("keycloakId", keycloakId).firstResult();
        if (company == null) {
            throw new BadRequestException("Empresa não encontrada com o Keycloak ID fornecido.");
        }

        long total = customerRepository.count("company.id", company.getId());

        List<Customer> customers = customerRepository.find("company.id", company.getId())
                .page(Page.of(page, size))
                .list();

        List<CustomerResponseDTO> items = customers.stream()
                .map(customer -> {
                    boolean enabled = keycloakService.isUserEnabled(customer.getKeycloakId());
                    return toDTO(customer, enabled);
                })
                .collect(Collectors.toList());

        return new PagedResult<>(items, total, page, size);
    }

    // buscando cliente por ID do Keycloak
    public Optional<Customer> findByKeycloakId(String keycloakId) {
        if (keycloakId == null || keycloakId.isEmpty()) {
            throw new BadRequestException("O Keycloak ID não pode ser nulo ou vazio.");
        }
        return customerRepository.find("keycloakId", keycloakId).firstResultOptional();
    }


}
