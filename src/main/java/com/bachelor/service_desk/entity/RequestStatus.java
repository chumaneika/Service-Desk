package com.bachelor.service_desk.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestStatus {
    CREATED("Новый запрос"),
    IN_PROGRESS("В процессе выполнения"),
    COMPLETED("Выполнен");

    private final String displayName;
}
