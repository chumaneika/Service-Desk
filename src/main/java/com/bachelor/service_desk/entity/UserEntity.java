package com.bachelor.service_desk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "user_seq",
            allocationSize = 10
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private List<RequestEntity> createdRequests = new ArrayList<>();

    @OneToMany(mappedBy = "responsible")
    @JsonIgnore
    private List<RequestEntity> responsibleRequests = new ArrayList<>();

    @Column(name = "number_phone", nullable = false, unique = true)
    private String numberPhone;

    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<ReviewEntity> reviews;

    @Column(name = "refresh_token_hash")
    @JsonIgnore
    private String refreshTokenHash;

    @Column(name = "refresh_token_expires_at")
    @JsonIgnore
    private Instant refreshTokenExpiresAt;

    @Column(name = "enabled")
    private boolean enabled;

    public UserEntity(String name, String surname, Role role, List<RequestEntity> requests, String numberPhone) {
        this.name = name;
        this.surname = surname;
        this.role = role;
        assignRequestsByRole(role, requests);
        this.numberPhone = numberPhone;
    }

    private void assignRequestsByRole(Role role, List<RequestEntity> requests) {
        if (requests == null) {
            return;
        }

        if (role == Role.USER) {
            this.createdRequests = requests;
        } else if (role == Role.ADMIN) {
            this.responsibleRequests = requests;
        }
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeSurname(String surname) {
        this.surname = surname;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changeNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void rotateRefreshToken(String refreshTokenHash, Instant refreshTokenExpiresAt) {
        this.refreshTokenHash = refreshTokenHash;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}
