package com.bachelor.service_desk.dto.security;

public record AuthResponseDTO(
        String token,
        String refreshToken,
        String type,
        Long userId,
        String numberPhone,
        String name,
        String surname
) {
    public AuthResponseDTO(String token, String refreshToken, Long userId, String numberPhone, String name, String surname) {
        this(token, refreshToken, "Bearer", userId, numberPhone, name, surname);
    }
}
