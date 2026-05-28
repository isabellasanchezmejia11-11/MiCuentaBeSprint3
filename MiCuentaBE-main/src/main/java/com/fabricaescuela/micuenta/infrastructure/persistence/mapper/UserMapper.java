package com.fabricaescuela.micuenta.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.infrastructure.persistence.entity.UserEntity;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getLastname(),
                entity.getEmail(),
                entity.getPasswordHash()
        );
    }

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.name(),
                user.lastname(),
                user.email(),
                user.passwordHash()
        );
    }
}