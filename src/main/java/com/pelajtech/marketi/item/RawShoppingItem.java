package com.pelajtech.marketi.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RawShoppingItem(
        String rawId,
        String marketId,
        String name,
        String description,
        String countryOfOrigin,
        String stockUnit,
        String barcode,
        LocalDateTime crawledAt,
        BigDecimal price,
        BigDecimal salePrice,
        int quantity,
        int stock
) { }
