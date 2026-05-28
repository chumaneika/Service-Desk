package com.bachelor.service_desk.service;

import com.bachelor.service_desk.dto.UserCreateDTO;
import com.bachelor.service_desk.dto.UserUpdateFullNameDTO;
import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    // Старший админ - создание пользователя
    public UserEntity createUser(UserCreateDTO dto) {
        boolean exists = userRepository.existsByNumberPhone(dto.numberPhone());

        if (exists) {
            throw new IllegalArgumentException("User with this phone number already exists");
        }

        UserEntity user = new UserEntity();

        user.changeName(dto.name());
        user.changeSurname(dto.surname());
        user.changeRole(dto.role());
        user.changeNumberPhone(dto.numberPhone());
        user.changeEnabled(true);

        String passwordHash = passwordEncoder.encode(dto.password());

        user.changePassword(passwordHash);

        return userRepository.save(user);
    }

    @Transactional
    // Пользователь - изменение имени и фамилии
    public UserEntity updateFullName(UserUpdateFullNameDTO dto) {
        UserEntity user = userRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        user.changeName(dto.name());
        user.changeSurname(dto.surname());

        return userRepository.save(user);
    }

    @Transactional
    // Старший админ и система безопасности - поиск по ID
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    @Transactional
    // Старший админ - получение пользователей по роли (только ADMIN или USER)
    public List<UserEntity> findAllByRole(Role role) {
        if (role != Role.ADMIN && role != Role.USER) {
            throw new IllegalArgumentException("Role must be ADMIN or USER");
        }
        return userRepository.findAllByRole(role);
    }

    @Transactional
    // Система безопасности - изменения токена переобновления
    public void rotateRefreshToken(Long userId, String refreshToken, Instant refreshTokenExpiresAt) {
        UserEntity user = findById(userId);
        user.rotateRefreshToken(hashToken(refreshToken), refreshTokenExpiresAt);
        userRepository.save(user);
    }

    @Transactional
    // Система безопасности - проверка валидации токена переобновления
    public boolean isRefreshTokenValid(Long accountId, String refreshToken) {
        UserEntity user = findById(accountId);
        if (user.getRefreshTokenHash() == null || user.getRefreshTokenExpiresAt() == null) {
            return false;
        }
        if (user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            return false;
        }
        return user.getRefreshTokenHash().equals(hashToken(refreshToken));
    }

    @Transactional
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll().stream()
                .filter(x -> x.getRole() != Role.SUPER_ADMIN)
                .toList();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }
}
