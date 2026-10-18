package com.shopfront.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryDto(
        Long id,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[a-z0-9-]+") String slug) {
}
