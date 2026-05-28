package com.fabricaescuela.micuenta.application.dto.request;

import java.math.BigDecimal;

public record AddContributionRequest(
        BigDecimal amount,
        String description
) {}
