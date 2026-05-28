package com.fabricaescuela.micuenta.application.port.out;

public interface PasswordHasher {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}