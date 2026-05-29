package com.bachelor.service_desk.controller;

import com.bachelor.service_desk.dto.UserCreateDTO;
import com.bachelor.service_desk.dto.UserUpdateFullNameDTO;
import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserEntity> updateFullName(@RequestBody @Valid UserUpdateFullNameDTO dto) {
        return ResponseEntity.ok(userService.updateFullName(dto));
    }

    @GetMapping("/id/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserEntity> findById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @GetMapping("/numberPhone/{numberPhone}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserEntity> findByNumberPhone(@PathVariable String numberPhone) {
        return ResponseEntity.ok(userService.findByNumberPhone(numberPhone));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserEntity>> searchUsers(@RequestParam String search) {
        return ResponseEntity.ok(userService.searchUsers(search));
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserEntity>> findAllByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.findAllByRole(role));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserEntity>> findAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }
}
