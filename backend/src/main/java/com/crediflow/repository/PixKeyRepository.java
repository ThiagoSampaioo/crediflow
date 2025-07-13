package com.crediflow.repository;

import com.crediflow.entity.PixKey;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PixKeyRepository implements PanacheRepository<PixKey> {

    public PixKey findByKey(String pixKey) {
        return find("pixKey", pixKey).firstResult();
    }

    public List<PixKey> findByBankAccountId(Long bankAccountId) {
        return list("bankAccount.id", bankAccountId);
    }
}
