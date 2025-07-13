package com.crediflow.repository;

import com.crediflow.entity.BankAccount;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BankAccountRepository implements PanacheRepository<BankAccount> {
    public BankAccount findByCustomerId(Long customerId) {
        return find("customer.id", customerId).firstResult();
    }

    public BankAccount findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber).firstResult();
    }

    public BankAccount findByAgencyAndAccount(String agency, String account) {
        return find("agencyNumber = ?1 and accountNumber = ?2", agency, account).firstResult();
    }

    public BankAccount findByPixKey(String key) {
        return getEntityManager()
                .createQuery("SELECT p.bankAccount FROM PixKey p WHERE p.pixKey = :key", BankAccount.class)
                .setParameter("key", key)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

}
