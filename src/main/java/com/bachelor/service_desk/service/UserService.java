package com.bachelor.service_desk.service;

import com.bachelor.service_desk.dto.UserCreateDTO;
import com.bachelor.service_desk.dto.UserUpdateFullNameDTO;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            throw new IllegalArgumentException(
                    "User with this phone number already exists"
            );
        }

        UserEntity user = new UserEntity();

        user.changeName(dto.name());
        user.changeSurname(dto.surname());
        user.changeRole(dto.role());
        user.changeNumberPhone(dto.numberPhone());

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
}
