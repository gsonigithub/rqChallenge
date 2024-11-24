package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

@Validated
public record EmpRequestDto(
        @NotNull @NotEmpty String name,
        @NotNull @Positive Integer salary,
        @NotNull @Min(16) @Max(75) Integer age,
        @NotNull String title,
        @NotNull @Email String email) {}
