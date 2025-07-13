package com.crediflow.repository;

import com.crediflow.entity.Customer;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {
    public boolean existsByCpf(String cpf) {
        return find("cpf", cpf).firstResultOptional().isPresent();
    }

    public boolean existsByEmail(String email) {
        return find("email", email).firstResultOptional().isPresent();
    }

    public PanacheQuery<Customer> searchPaged(String query, int page, int size) {
        String lowerQuery = "%" + query.toLowerCase() + "%";
        return find("LOWER(name) LIKE ?1 OR LOWER(cpf) LIKE ?1 OR LOWER(email) LIKE ?1", lowerQuery)
                .page(Page.of(page, size));
    }

}
