package com.bachelor.service_desk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateDTO(
        @NotBlank
        @Size(min = 3, max = 150)
        String title,

        @NotBlank
        @Size(min = 10, max = 2000)
        String description,

        @NotNull
        Long owner,

        @NotNull
        Long request
) {}
