package com.pelajtech.marketi.item;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ItemHelpers {

    private ItemHelpers() {
    }

    public static ShoppingItem item(String group, String name) {
        return new ShoppingItem(
                UUID.randomUUID(),
                group,
                name,
                Set.of("market"),
                Map.of("market", 1),
                Map.of(),
                "Kosovo",
                "barcode-" + name,
                ShoppingItem.Unit.PIECE,
                new ShoppingItem.Quantity(1, 1)
        );
    }

}
