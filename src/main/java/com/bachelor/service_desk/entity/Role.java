package com.bachelor.service_desk.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("Пользователь"),
    ADMIN("Админ"),
    SUPER_ADMIN("Старший админ");

    private final String displayName;
}