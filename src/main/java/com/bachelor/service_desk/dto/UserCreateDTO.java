package com.bachelor.service_desk.dto;

import com.bachelor.service_desk.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(

        @NotBlank
        @Size(min = 2, max = 50)
        String name,

        @NotBlank
        @Size(min = 2, max = 50)
        String surname,

        @NotNull
        Role role,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @NotBlank
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number format"
        )
        String numberPhone

) {
}