package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.heuristics.NameHeuristics;
import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemReducer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class LocalItemReducer implements ItemReducer {

    private final LocalItemStore store;

    LocalItemReducer(LocalItemStore store) {
        this.store = store;
    }

    @Override
    public void reduce(Collection<String> keys, Consumer<ShoppingItem> sink) {
        Objects.requireNonNull(keys, "keys");
        Objects.requireNonNull(sink, "sink");

        var items = store.items(keys);
        var names = items.stream()
                .map(ShoppingItem::name)
                .toList();

        var targetName = NameHeuristics.NAME_HEURISTIC.highest(names).orElse("");

        items.stream()
                .filter(item -> item.name().equals(targetName))
                .findFirst().ifPresent(sink);

    }

    @Override
    public Set<String> claim() {
        return store.claim();
    }

}
