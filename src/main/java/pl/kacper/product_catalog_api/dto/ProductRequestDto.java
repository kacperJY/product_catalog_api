package pl.kacper.product_catalog_api.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank @Length(max = 150)  String name,
        String description,
        @Positive @NotNull @Digits(integer = 36,fraction = 2) BigDecimal price,
        @PositiveOrZero @NotNull Integer stockQuantity
) {
}
