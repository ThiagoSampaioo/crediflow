package com.crediflow.entity;

import com.crediflow.enums.PixKeyType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pix_keys")
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pixKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixKeyType keyType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return pixKey;
    }

    public void setKey(String key) {
        this.pixKey = key;
    }

    public PixKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(PixKeyType keyType) {
        this.keyType = keyType;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
