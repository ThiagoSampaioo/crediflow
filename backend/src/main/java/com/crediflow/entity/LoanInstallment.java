// Entidade LoanInstallment.java
package com.crediflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_installments")
public class LoanInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proposal_id")
    private LoanProposal proposal;

    private Integer number;
    private BigDecimal value;
    private LocalDate dueDate;
    private BigDecimal iof;
    private BigDecimal balance;
    private BigDecimal interest;
    private BigDecimal principal;
    private BigDecimal additionalValue;
    private BigDecimal presentValue;
    private String status;
    private Integer period;
    private Integer daysElapsed;
    private Integer daysLate;
    private BigDecimal paidValue;
    @Column(nullable = false)
    private boolean paid = false;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanProposal getProposal() {
        return proposal;
    }

    public void setProposal(LoanProposal proposal) {
        this.proposal = proposal;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getIof() {
        return iof;
    }

    public void setIof(BigDecimal iof) {
        this.iof = iof;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAdditionalValue() {
        return additionalValue;
    }

    public void setAdditionalValue(BigDecimal additionalValue) {
        this.additionalValue = additionalValue;
    }

    public BigDecimal getPresentValue() {
        return presentValue;
    }

    public void setPresentValue(BigDecimal presentValue) {
        this.presentValue = presentValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(Integer daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    public Integer getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(Integer daysLate) {
        this.daysLate = daysLate;
    }

    public BigDecimal getPaidValue() {
        return paidValue;
    }

    public void setPaidValue(BigDecimal paidValue) {
        this.paidValue = paidValue;
    }

    public boolean isPaid() {
        return paid;
    }
    
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

}
