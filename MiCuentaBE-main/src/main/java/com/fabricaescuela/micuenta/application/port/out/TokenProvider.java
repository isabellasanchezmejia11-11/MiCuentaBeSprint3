package com.fabricaescuela.micuenta.application.port.out;

public interface TokenProvider {
    String generateToken(String subject);
    String extractSubject(String token);
    boolean isTokenValid(String token, String subject);
}