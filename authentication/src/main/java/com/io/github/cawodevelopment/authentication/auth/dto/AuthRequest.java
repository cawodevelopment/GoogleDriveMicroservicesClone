package com.io.github.cawodevelopment.authentication.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AuthRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Length(min = 8)
        String password
){}