package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.ShoppingItem;

import java.util.Optional;
import java.util.function.Consumer;

public interface ItemLog {

    void append(ShoppingItem item, Consumer<ShoppingItem> sink);

    default void append(ShoppingItem item) {
        append(item, _ -> {});
    }

    void commit();

    interface Factory {
        Optional<ItemLog> create();
    }

}
