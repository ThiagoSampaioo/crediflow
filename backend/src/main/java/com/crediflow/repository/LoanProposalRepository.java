package com.crediflow.repository;

import java.util.Optional;

import com.crediflow.entity.LoanProposal;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class LoanProposalRepository implements PanacheRepositoryBase<LoanProposal, Long> {
   
    @Inject
    EntityManager em;
    // Isso serve para acessar o EntityManager diretamente, se necessário
    public Optional<LoanProposal> findByIdOptional(Long id) {
        LoanProposal proposal = em.find(LoanProposal.class, id);
        return Optional.ofNullable(proposal);
    }
}
