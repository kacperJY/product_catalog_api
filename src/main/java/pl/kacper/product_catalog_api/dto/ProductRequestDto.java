package pl.kacper.product_catalog_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank @Length(max = 150)  String name,
        String description,
        @Positive @NotNull BigDecimal price,
        @PositiveOrZero @NotNull Integer stockQuantity
) {
}
