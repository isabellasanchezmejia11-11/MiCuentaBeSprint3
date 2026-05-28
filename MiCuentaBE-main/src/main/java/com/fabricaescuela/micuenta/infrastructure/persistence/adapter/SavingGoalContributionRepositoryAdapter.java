package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.SavingGoalContribution;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalContributionRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.SavingGoalContributionEntity;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.SavingGoalContributionJpaRepository;

@Repository
public class SavingGoalContributionRepositoryAdapter implements SavingGoalContributionRepository {

    private final SavingGoalContributionJpaRepository jpaRepository;

    public SavingGoalContributionRepositoryAdapter(SavingGoalContributionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SavingGoalContribution save(SavingGoalContribution contribution) {
        SavingGoalContributionEntity entity = toEntity(contribution);
        SavingGoalContributionEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SavingGoalContribution> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<SavingGoalContribution> findBySavingGoalId(Long savingGoalId) {
        return jpaRepository.findBySavingGoalId(savingGoalId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private SavingGoalContribution toDomain(SavingGoalContributionEntity entity) {
        return new SavingGoalContribution(
                entity.getId(),
                entity.getSavingGoalId(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    private SavingGoalContributionEntity toEntity(SavingGoalContribution domain) {
        return new SavingGoalContributionEntity(
                domain.id(),
                domain.savingGoalId(),
                domain.amount(),
                domain.description(),
                domain.createdAt()
        );
    }
}
