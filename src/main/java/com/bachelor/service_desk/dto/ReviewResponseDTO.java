package com.bachelor.service_desk.dto;

public record ReviewResponseDTO(
        Long id,
        String title,
        String description,
        Long ownerId,
        Long requestId
) {}
