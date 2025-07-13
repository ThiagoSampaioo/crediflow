package com.crediflow.repository;

import com.crediflow.dto.PagedResult;
import com.crediflow.entity.BankAccount;
import com.crediflow.entity.Transaction;
import com.crediflow.enums.TransactionStatus;
import com.crediflow.enums.TransactionType;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> findByAccountId(Long accountId) {
        return find("fromAccount.id = ?1 or toAccount.id = ?1", accountId).list();
    }

    public PagedResult<Transaction> searchPaged(Long accountId, TransactionType type, TransactionStatus status,
            int page, int size) {

        StringBuilder query = new StringBuilder("(fromAccount.id = :id or toAccount.id = :id)");
        Map<String, Object> params = new HashMap<>();
        params.put("id", accountId);

        if (type != null) {
            query.append(" and type = :type");
            params.put("type", type);
        }

        if (status != null) {
            query.append(" and status = :status");
            params.put("status", status);
        }

        long total = count(query.toString(), params);

        List<Transaction> items = find(query.toString(), Sort.by("createdAt").descending(), params)
                .page(page, size)
                .list();

        return new PagedResult<>(items, total, page, size);
    }

}
