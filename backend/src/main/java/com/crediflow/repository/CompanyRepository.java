package com.crediflow.repository;

import com.crediflow.entity.Company;
import com.crediflow.entity.Customer;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {
    public boolean existsByCnpj(String cnpj) {
        return find("cnpj", cnpj).firstResultOptional().isPresent();
    }

    public boolean existsByEmail(String email) {
        return find("email", email).firstResultOptional().isPresent();
    }

    public boolean existsByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional().isPresent();
    }

    public PanacheQuery<Company> searchPaged(String query, int page, int size) {
        String lowerQuery = "%" + query.toLowerCase() + "%";
        return find("LOWER(name) LIKE ?1 OR LOWER(cnpj) LIKE ?1 OR LOWER(email) LIKE ?1", lowerQuery)
                .page(Page.of(page, size));
    }
}
