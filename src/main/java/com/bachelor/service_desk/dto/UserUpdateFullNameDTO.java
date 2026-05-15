package com.bachelor.service_desk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateFullNameDTO(

        @NotNull
        Long id,

        @NotBlank
        @Size(min = 2, max = 50)
        String name,

        @NotBlank
        @Size(min = 2, max = 50)
        String surname
) {}
