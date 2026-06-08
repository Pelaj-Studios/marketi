package com.pelajtech.marketi.item;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ShoppingItem(
        UUID id,

        String group,
        String name,

        Set<String> markets,
        Map<String, Integer> marketStockCount,

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

    record Price(String market, BigDecimal price, @Nullable BigDecimal salePrice) {

    }

    record Quantity(int multiplier, int unit) {

    }
}
