package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class LocalItemLog implements ItemLog {

    private final LocalItemStore store;
    private final List<ShoppingItem> items = new ArrayList<>();

    LocalItemLog(LocalItemStore store) {
        this.store = store;
    }

    public static LocalItemLogFactory factory() {
        return new LocalItemLogFactory();
    }

    @Override
    public void append(ShoppingItem item, Consumer<ShoppingItem> sink) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(sink, "sink");
        Objects.requireNonNull(item.group(), "item group");

        items.add(item);
        sink.accept(item);
    }

    @Override
    public void commit() {
        store.append(items);
        items.clear();
    }

    public static class LocalItemLogFactory implements ItemLog.Factory {

        private final LocalItemStore store;

        public LocalItemLogFactory() {
            this(new LocalItemStore());
        }

        LocalItemLogFactory(LocalItemStore store) {
            this.store = store;
        }

        @Override
        public Optional<ItemLog> create() {
            return Optional.of(new LocalItemLog(store));
        }

        public LocalItemReducer reducer() {
            return new LocalItemReducer(store);
        }

    }

}
