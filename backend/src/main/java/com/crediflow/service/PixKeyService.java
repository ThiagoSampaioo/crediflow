package com.crediflow.service;

import com.crediflow.entity.PixKey;
import com.crediflow.entity.BankAccount;
import com.crediflow.enums.PixKeyType;
import com.crediflow.repository.BankAccountRepository;
import com.crediflow.repository.PixKeyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class PixKeyService {

    @Inject
    PixKeyRepository pixKeyRepository;

    @Inject
    BankAccountRepository bankAccountRepository;

    @Transactional  
    public PixKey registerPixKey(Long bankAccountId, String pixKey, PixKeyType keyType) {
        if (pixKeyRepository.findByKey(pixKey) != null) {
            throw new IllegalArgumentException("Chave Pix já cadastrada");
        }

        BankAccount account = bankAccountRepository.findById(bankAccountId);
        if (account == null) {
            throw new IllegalArgumentException("Conta bancária não encontrada");
        }

        PixKey pixKey_ = new PixKey();
        pixKey_.setKey(pixKey);
        pixKey_.setKeyType(keyType);
        pixKey_.setBankAccount(account);
        pixKey_.setCreatedAt(LocalDateTime.now());

        pixKeyRepository.persist(pixKey_);
        return pixKey_;
    }
   @Transactional 
    public PixKey getById(Long id) {
        PixKey pixKey = pixKeyRepository.findById(id);
        if (pixKey == null) {
            throw new IllegalArgumentException("Chave Pix não encontrada");
        }
        return pixKey;
    }

    @Transactional
    public PixKey updatePixKey(Long id, Long bankAccountId, String pixKey, PixKeyType keyType) {
        PixKey existing = pixKeyRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Chave Pix não encontrada");
        }

        // Verifica duplicação se a chave mudou
        if (!existing.getKey().equals(pixKey) && pixKeyRepository.findByKey(pixKey) != null) {
            throw new IllegalArgumentException("Nova chave Pix já está em uso");
        }

        BankAccount account = bankAccountRepository.findById(bankAccountId);
        if (account == null) {
            throw new IllegalArgumentException("Conta bancária não encontrada");
        }

        existing.setKey(pixKey);
        existing.setKeyType(keyType);
        existing.setBankAccount(account);
        pixKeyRepository.persist(existing);

        return existing;
    }

    public List<PixKey> getAllByAccount(Long bankAccountId) {
        return pixKeyRepository.findByBankAccountId(bankAccountId);
    }

    @Transactional
    public void deletePixKey(Long id) {
        PixKey pixKey = pixKeyRepository.findById(id);
        if (pixKey == null) {
            throw new IllegalArgumentException("Chave Pix não encontrada");
        }
        pixKeyRepository.delete(pixKey);
    }

    
}
