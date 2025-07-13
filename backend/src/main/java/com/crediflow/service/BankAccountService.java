package com.crediflow.service;

import com.crediflow.dto.AccountRecipientInfoDTO;
import com.crediflow.dto.BankAccountResponseDTO;
import com.crediflow.entity.BankAccount;
import com.crediflow.entity.Customer;
import com.crediflow.entity.PixKey;
import com.crediflow.exception.BadRequestException;
import com.crediflow.exception.ConflictException;
import com.crediflow.repository.BankAccountRepository;
import com.crediflow.repository.CustomerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@ApplicationScoped
public class BankAccountService {

    @Inject
    BankAccountRepository bankAccountRepository;

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    public BankAccount createAccountForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new ConflictException("Cliente não encontrado");
        }

        if (bankAccountRepository.findByCustomerId(customerId) != null) {
            throw new ConflictException("Cliente já possui uma conta bancária");
        }

        BankAccount account = new BankAccount();
        account.setAccountNumber(generateRandomAccountNumber());
        account.setAgencyNumber("0001");
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(customer);
        account.setCreatedAt(LocalDateTime.now());

        bankAccountRepository.persist(account);

        return account;
    }

    private String generateRandomAccountNumber() {
        Random random = new Random();
        return String.format("%08d", random.nextInt(100_000_000));
    }

    public BankAccount getAccountByCustomerId(Long customerId) {
        return bankAccountRepository.findByCustomerId(customerId);
    }

    public BankAccount getAccountByAccountNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber);
    }

    public void deleteAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId);
        if (account == null) {
            throw new BadRequestException("Conta bancária não encontrada");
        }

        bankAccountRepository.delete(account);
    }

    public BankAccount getAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId);
    }

    // Método utilitário (privado) usado apenas internamente
    public void changeBalance(BankAccount account, BigDecimal delta) {
        if (account == null) {
            throw new BadRequestException("Conta bancária não encontrada");
        }
        account.setBalance(account.getBalance().add(delta));
        account.setUpdatedAt(LocalDateTime.now());
        bankAccountRepository.persist(account);
    }

    public BankAccount updateAccount(BankAccount account) {
        BankAccount existingAccount = bankAccountRepository.findById(account.getId());
        if (existingAccount == null) {
            throw new BadRequestException("Conta bancária não encontrada");
        }

        existingAccount.setAccountNumber(account.getAccountNumber());
        existingAccount.setAgencyNumber(account.getAgencyNumber());
        existingAccount.setUpdatedAt(LocalDateTime.now());
        bankAccountRepository.persist(existingAccount);

        return existingAccount;
    }


    public AccountRecipientInfoDTO findRecipientByAgencyAndAccount(String agency, String account) {
        BankAccount accountFound = bankAccountRepository.findByAgencyAndAccount(agency, account);
        if (accountFound == null) {
            throw new BadRequestException("Conta bancária não encontrada");
        }

        PixKey pix = accountFound.getPixKeys() != null && !accountFound.getPixKeys().isEmpty()
            ? accountFound.getPixKeys().get(0)
            : null;

        return new AccountRecipientInfoDTO(
            accountFound.getCustomer().getName(),
            accountFound.getAgencyNumber(),
            accountFound.getAccountNumber(),
            pix != null ? pix.getKey() : null,
            pix != null ? pix.getKeyType().name() : null
        );
    }

    public AccountRecipientInfoDTO findRecipientByPixKey(String pixKey) {
        BankAccount account = bankAccountRepository.findByPixKey(pixKey);
        if (account == null) {
            throw new BadRequestException("Conta bancária não encontrada");
        }

        PixKey pix = account.getPixKeys().stream()
            .filter(p -> p.getKey().equals(pixKey))
            .findFirst()
            .orElse(null);

        return new AccountRecipientInfoDTO(
            account.getCustomer().getName(),
            account.getAgencyNumber(),
            account.getAccountNumber(),
            pix != null ? pix.getKey() : null,
            pix != null ? pix.getKeyType().name() : null
        );
    }



}
