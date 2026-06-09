package com.pelajtech.marketi.item;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ItemHelpers {

    private ItemHelpers() {
    }

    public static ShoppingItem item(String group, String name) {
        return item(group, name, "market", BigDecimal.ONE, BigDecimal.ONE);
    }

    public static ShoppingItem item(String group, String name, String market, BigDecimal stock, BigDecimal price) {
        return new ShoppingItem(
                UUID.randomUUID(),
                group,
                name,
                Set.of(market),
                Map.of(market, stock),
                Map.of(market, new ShoppingItem.Price(market, price, Optional.empty())),
                "Kosovo",
                "barcode-" + name,
                ShoppingItem.Unit.PIECE,
                new ShoppingItem.Quantity(BigDecimal.ONE, BigDecimal.ONE)
        );
    }

}
