package com.shopfront.catalog.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Request and response bodies of the product endpoints. */
public final class ProductDtos {

    private ProductDtos() {
    }

    public record Money(
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal amount,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {
    }

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            String description,
            Long categoryId,
            boolean active,
            Money price) {
    }

    public record ProductPage(List<ProductResponse> items, long total, int page, int pageSize) {
    }

    public record CreateProductRequest(
            @NotBlank @Size(max = 64) String sku,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 2000) String description,
            Long categoryId,
            @Valid Money price) {
    }

    public record UpdateProductRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 2000) String description,
            Long categoryId,
            boolean active) {
    }

    public record PriceResponse(Long id, BigDecimal amount, String currency, Instant validFrom, Instant validTo) {
    }

    public record CreatePriceRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal amount,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
            Instant validFrom) {
    }
}
