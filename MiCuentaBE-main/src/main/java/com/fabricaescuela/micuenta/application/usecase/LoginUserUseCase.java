package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.request.LoginRequest;
import com.fabricaescuela.micuenta.application.dto.response.AuthResponse;
import com.fabricaescuela.micuenta.application.exception.InvalidCredentialsException;
import com.fabricaescuela.micuenta.application.port.out.PasswordHasher;
import com.fabricaescuela.micuenta.application.port.out.TokenProvider;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        boolean matches = passwordHasher.matches(request.password(), user.passwordHash());
        if (!matches) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = tokenProvider.generateToken(user.email());

        return new AuthResponse(token, "Bearer");
    }
}