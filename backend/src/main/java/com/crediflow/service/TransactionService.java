package com.crediflow.service;

import com.crediflow.entity.Transaction;
import com.crediflow.dto.PagedResult;
import com.crediflow.dto.TransactionResponseDTO;
import com.crediflow.entity.BankAccount;
import com.crediflow.entity.LoanInstallment;
import com.crediflow.enums.TransactionStatus;
import com.crediflow.enums.TransactionType;
import com.crediflow.exception.BadRequestException;
import com.crediflow.repository.BankAccountRepository;
import com.crediflow.repository.LoanInstallmentRepository;
import com.crediflow.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    BankAccountRepository bankAccountRepository;

    @Inject
    TransactionPDFService transactionPDFService;

    @Inject
    LoanInstallmentRepository installmentRepository;

    @Transactional
    public Transaction transfer(Long fromId, Long toId, BigDecimal amount, TransactionType type, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor da transação deve ser positivo");
        }

        BankAccount from = bankAccountRepository.findById(fromId);
        BankAccount to = bankAccountRepository.findById(toId);

        if (from == null || to == null) {
            throw new BadRequestException("Conta de origem ou destino inválida");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(description);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.persist(tx);
        return tx;
    }

    @Transactional
    public Transaction deposit(Long toId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor do depósito deve ser positivo");
        }

        BankAccount to = bankAccountRepository.findById(toId);
        if (to == null) {
            throw new BadRequestException("Conta destino inválida");
        }

        to.setBalance(to.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setFromAccount(null); // Externo
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(description != null ? description : "Depósito");
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.persist(tx);
        return tx;
    }

    @Transactional
    public Transaction pay(Long fromId, BigDecimal amount, String description, TransactionType type) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor do pagamento deve ser positivo");
        }

        BankAccount from = bankAccountRepository.findById(fromId);
        if (from == null) {
            throw new BadRequestException("Conta de origem inválida");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }

        from.setBalance(from.getBalance().subtract(amount));

        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(null); // Pagamento externo
        tx.setAmount(amount);
        tx.setType(type); // Ex: LOAN_PAYMENT, BOLETO_PAYMENT
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(description != null ? description : "Pagamento");
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.persist(tx);
        return tx;
    }

    public List<Transaction> listAll() {
        return transactionRepository.listAll();
    }

    public Transaction getById(Long id) {
        Transaction tx = transactionRepository.find("id", id).firstResult();
        if (tx == null) {
            throw new BadRequestException("Transação não encontrada");
        }

        return tx;
    }

    public PagedResult<TransactionResponseDTO> searchTransactions(Long accountId, TransactionType type,
            TransactionStatus status, int page, int size) {
        PagedResult<Transaction> paged = transactionRepository.searchPaged(accountId, type, status, page, size);
        List<TransactionResponseDTO> dtos = paged.getItems().stream()
                .map(TransactionResponseDTO::fromEntity)
                .toList();

        return new PagedResult<>(dtos, paged.getTotal(), paged.getPage(), paged.getSize());
    }

    @Transactional
    public byte[] payInstallmentAndReturnReceipt(Long installmentId, Long bankAccountId) {
        // Lógica já validada
        LoanInstallment installment = installmentRepository.findByIdOptional(installmentId)
                .orElseThrow(() -> new BadRequestException("Parcela não encontrada"));

        if (installment.getPaidValue() != null && installment.getPaidValue().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Parcela já foi paga");
        }

        BankAccount account = bankAccountRepository.findByIdOptional(bankAccountId)
                .orElseThrow(() -> new BadRequestException("Conta bancária não encontrada"));

        if (account.getBalance().compareTo(installment.getValue()) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance().subtract(installment.getValue()));
        bankAccountRepository.persist(account);

        installment.setPaidValue(installment.getValue());
        installment.setPaid(true);
        installmentRepository.persist(installment);

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setToAccount(null); // conta do sistema
        tx.setAmount(installment.getValue());
        tx.setType(TransactionType.LOAN_INSTALLMENT_PAYMENT);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription("Pagamento da parcela #" + installment.getNumber());
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.persist(tx);

        return transactionPDFService.generateReceipt(tx); // novo serviço
    }

    @Transactional
    public Transaction transferByAgencyAccount(Long fromId, String agency, String account, BigDecimal amount,
            String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valor inválido");
        }

        BankAccount from = bankAccountRepository.findById(fromId);
        BankAccount to = bankAccountRepository.find("agencyNumber = ?1 and accountNumber = ?2", agency, account)
                .firstResult();

        if (from == null || to == null) {
            throw new BadRequestException("Conta origem ou destino inválida");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(description);
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.persist(tx);
        return tx;
    }

    @Transactional
    public Transaction depositByAgencyAccount(String toAgency, String toAccount, BigDecimal amount,
            String description) {
        BankAccount to = bankAccountRepository.findByAgencyAndAccount(toAgency, toAccount);
        if (to == null)
            throw new BadRequestException("Conta de destino não encontrada");
        return deposit(to.getId(), amount, description);
    }

    @Transactional
    public Transaction pixTransfer(Long fromId, String pixKey, BigDecimal amount, String description) {
        BankAccount to = bankAccountRepository.findByPixKey(pixKey);
        if (to == null)
            throw new BadRequestException("Chave Pix não encontrada");

        return transfer(fromId, to.getId(), amount, TransactionType.PIX,
                description != null ? description : "Transferência via Pix");
    }


}
