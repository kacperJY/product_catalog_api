package pl.kacper.product_catalog_api.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity
) {
}
