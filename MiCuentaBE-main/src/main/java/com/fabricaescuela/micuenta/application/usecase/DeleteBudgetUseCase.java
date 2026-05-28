package com.fabricaescuela.micuenta.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fabricaescuela.micuenta.application.exception.ResourceNotFoundException;
import com.fabricaescuela.micuenta.domain.model.Budget;
import com.fabricaescuela.micuenta.domain.model.User;
import com.fabricaescuela.micuenta.domain.repository.BudgetRepository;
import com.fabricaescuela.micuenta.domain.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class DeleteBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public DeleteBudgetUseCase(
            BudgetRepository budgetRepository,
            UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(Long budgetId, String authenticatedEmail) {
        // Validar usuario autenticado
        User user = userRepository.findByEmail(authenticatedEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Obtener el presupuesto
        Optional<Budget> budgetOpt = budgetRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con id: " + budgetId);
        }

        Budget budget = budgetOpt.get();

        // Validar que el presupuesto pertenezca al usuario
        if (!budget.getUserId().equals(user.id())) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con id: " + budgetId);
        }

        // Eliminar el presupuesto
        budgetRepository.deleteById(budgetId);
    }
}
