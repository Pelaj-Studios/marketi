package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.heuristics.NameHeuristics;
import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemReducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class LocalItemReducer implements ItemReducer {

    private final LocalItemStore store;

    LocalItemReducer(LocalItemStore store) {
        this.store = store;
    }

    @Override
    public void reduceGroup(String group, Consumer<ShoppingItem> sink) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(sink, "sink");

        var items = store.items(group);
        if (items.isEmpty()) {
            return;
        }

        var targetItem = items.stream()
                .reduce((best, candidate) -> NameHeuristics.NAME_HEURISTIC.score(candidate.name())
                        > NameHeuristics.NAME_HEURISTIC.score(best.name())
                        ? candidate
                        : best
                )
                .orElseThrow();

        var markets = new HashSet<>(targetItem.markets());
        var marketStockCount = new HashMap<>(targetItem.marketStockCount());
        var marketPrices = new HashMap<>(targetItem.marketPrices());

        for (var candidate : items) {
            for (var market : candidate.markets()) {
                markets.add(market);
                if (candidate.marketStockCount().containsKey(market)) {
                    marketStockCount.putIfAbsent(market, candidate.marketStockCount().get(market));
                }
                if (candidate.marketPrices().containsKey(market)) {
                    marketPrices.putIfAbsent(market, candidate.marketPrices().get(market));
                }
            }
        }

        sink.accept(new ShoppingItem(
                targetItem.id(),
                targetItem.group(),
                targetItem.name(),
                markets,
                marketStockCount,
                marketPrices,
                targetItem.countryOfOrigin(),
                targetItem.barcode(),
                targetItem.unit(),
                targetItem.quantity()
        ));
    }

    @Override
    public Set<String> claimGroups() {
        return store.claimGroups();
    }

}
