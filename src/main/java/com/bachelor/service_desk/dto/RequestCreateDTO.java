package com.bachelor.service_desk.dto;

public record RequestCreateDTO(
        String title,
        String description,
        Long createdById
) {}
