package com.pelajtech.marketi.item;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record ShoppingItem(
        UUID id,

        String group,
        String name,

        Set<String> markets,
        Map<String, BigDecimal> marketStockCount,

        Map<String, Price> marketPrices,

        String countryOfOrigin,
        String barcode,

        Unit unit,
        Quantity quantity
) {

    public enum Unit {
        PIECE,
        LITRE,
        GRAM
    }

    public record Price(String market, BigDecimal price, Optional<BigDecimal> salePrice) {

    }

    public record Quantity(BigDecimal multiplier, BigDecimal unit) {

    }
}
