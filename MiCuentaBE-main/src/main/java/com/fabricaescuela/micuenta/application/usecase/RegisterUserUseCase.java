package com.fabricaescuela.micuenta.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabricaescuela.micuenta.application.dto.request.RegisterRequest;
import com.fabricaescuela.micuenta.application.dto.response.UserResponse;
import com.fabricaescuela.micuenta.application.exception.EmailAlreadyExistsException;
import com.fabricaescuela.micuenta.application.port.out.PasswordHasher;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.model.MovementType;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CreateCategoryUseCase createCategoryUseCase;

    public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher, CreateCategoryUseCase createCategoryUseCase) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.createCategoryUseCase = createCategoryUseCase;
    }

    @Transactional
    public UserResponse execute(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        String encodedPassword = passwordHasher.encode(request.password());

        User savedUser = userRepository.save(
                new User(
                        null,
                        request.name().trim(),
                        request.lastname().trim(),
                        normalizedEmail,
                        encodedPassword
                )
        );

        // Crear categorías por defecto para el nuevo usuario
        createDefaultCategories(normalizedEmail);

        return new UserResponse(savedUser.id(), savedUser.name(), savedUser.lastname(), savedUser.email());
    }

    private void createDefaultCategories(String email) {
        // INGRESOS
        createCategoryUseCase.execute(email, "Salario", MovementType.INCOME, "#B5EAD7");
        createCategoryUseCase.execute(email, "Inversiones", MovementType.INCOME, "#C1E1C1");
        createCategoryUseCase.execute(email, "Regalo", MovementType.INCOME, "#98FB98");
        createCategoryUseCase.execute(email, "Otros ingresos", MovementType.INCOME, "#E0FFF0");

        // GASTOS
        createCategoryUseCase.execute(email, "Alimentación", MovementType.EXPENSE, "#FFDAB9");
        createCategoryUseCase.execute(email, "Transporte", MovementType.EXPENSE, "#FFE4B5");
        createCategoryUseCase.execute(email, "Servicios", MovementType.EXPENSE, "#FFFACD");
        createCategoryUseCase.execute(email, "Entretenimiento", MovementType.EXPENSE, "#FFB6C1");
        createCategoryUseCase.execute(email, "Salud", MovementType.EXPENSE, "#FFCCCB");
        createCategoryUseCase.execute(email, "Educación", MovementType.EXPENSE, "#E6E6FA");
        createCategoryUseCase.execute(email, "Ropa", MovementType.EXPENSE, "#FFD700");
        createCategoryUseCase.execute(email, "Otros gastos", MovementType.EXPENSE, "#D3D3D3");
    }
}