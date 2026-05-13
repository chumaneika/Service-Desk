package com.bachelor.service_desk.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewOwner {
    GOOD("Выполнено успешно"),
    BAD("Выполнено плохо");

    private final String displayName;
}
