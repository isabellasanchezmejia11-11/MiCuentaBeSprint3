package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.SavingGoal;
import com.fabricaescuela.micuenta.domain.repository.SavingGoalRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.SavingGoalEntity;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.SavingGoalJpaRepository;

@Repository
public class SavingGoalRepositoryAdapter implements SavingGoalRepository {

    private final SavingGoalJpaRepository jpaRepository;

    public SavingGoalRepositoryAdapter(SavingGoalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SavingGoal save(SavingGoal savingGoal) {
        SavingGoalEntity entity = toEntity(savingGoal);
        SavingGoalEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SavingGoal> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<SavingGoal> findByUserIdAndArchivedFalse(Long userId) {
        return jpaRepository.findByUserIdAndArchivedFalse(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<SavingGoal> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public SavingGoal update(SavingGoal savingGoal) {
        SavingGoalEntity entity = toEntity(savingGoal);
        SavingGoalEntity updated = jpaRepository.save(entity);
        return toDomain(updated);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void archiveById(Long id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setArchived(true);
            jpaRepository.save(entity);
        });
    }

    private SavingGoal toDomain(SavingGoalEntity entity) {
        return new SavingGoal(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getTargetAmount(),
                entity.getCurrentAmount(),
                entity.isArchived(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private SavingGoalEntity toEntity(SavingGoal domain) {
        return new SavingGoalEntity(
                domain.id(),
                domain.userId(),
                domain.name(),
                domain.targetAmount(),
                domain.currentAmount(),
                domain.archived(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
