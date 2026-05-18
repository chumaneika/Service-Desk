package com.bachelor.service_desk.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDTO(
        @NotBlank
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number format"
        )
        String numberPhone,

        @NotBlank
        String password
) {}
