package com.pelajtech.marketi.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
        Optional<BigDecimal> salePrice,
        BigDecimal quantity,
        BigDecimal stock
) { }
