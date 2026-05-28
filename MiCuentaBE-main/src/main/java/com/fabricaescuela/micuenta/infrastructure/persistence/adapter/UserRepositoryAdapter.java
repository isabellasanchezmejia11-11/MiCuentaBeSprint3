package com.fabricaescuela.micuenta.infrastructure.persistence.adapter;



import org.springframework.stereotype.Repository;

import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.UserEntity;
import com.fabricaescuela.micuenta.infrastructure.persistence.mapper.UserMapper;
import com.fabricaescuela.micuenta.infrastructure.persistence.repository.UserJpaRepository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}