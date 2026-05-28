package com.fabricaescuela.micuenta.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabricaescuela.micuenta.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}