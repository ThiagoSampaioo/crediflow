package com.crediflow.repository;

import com.crediflow.entity.LoanInstallment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoanInstallmentRepository implements PanacheRepository<LoanInstallment> {
}
