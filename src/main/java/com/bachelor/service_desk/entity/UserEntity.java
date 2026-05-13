package com.bachelor.service_desk.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToMany(mappedBy = "user")
    private List<RequestEntity> requests;

    @Column(name = "number_phone", nullable = false, unique = true)
    private String numberPhone;

    public UserEntity(String name, String surname, Role role, List<RequestEntity> requests, String numberPhone) {
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.requests = requests;
        this.numberPhone = numberPhone;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeSurname(String surname) {
        this.surname = surname;
    }

    public void changeNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }
}
