package com.fabricaescuela.micuenta.domain.repository;

import java.util.Optional;

import com.fabricaescuela.micuenta.domain.model.User;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}