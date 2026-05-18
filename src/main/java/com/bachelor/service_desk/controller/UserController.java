package com.bachelor.service_desk.controller;

import com.bachelor.service_desk.dto.UserCreateDTO;
import com.bachelor.service_desk.dto.UserUpdateFullNameDTO;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserEntity> createUser(@RequestBody @Valid UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PutMapping("/full-name")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserEntity> updateFullName(@RequestBody @Valid UserUpdateFullNameDTO dto) {
        return ResponseEntity.ok(userService.updateFullName(dto));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserEntity> findById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }
}
