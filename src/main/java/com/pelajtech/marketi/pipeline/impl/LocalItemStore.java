package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.item.ShoppingItem;

import java.util.*;

final class LocalItemStore {

    private final Map<String, List<ShoppingItem>> itemsByGroup = new HashMap<>();
    private final Set<String> pendingGroups = new HashSet<>();

    synchronized void append(Collection<ShoppingItem> items) {
        for (var item : items) {
            itemsByGroup
                    .computeIfAbsent(item.group(), _ -> new ArrayList<>())
                    .add(item);
            pendingGroups.add(item.group());
        }
    }

    synchronized Set<String> claimGroups() {
        var claimed = Set.copyOf(pendingGroups);
        pendingGroups.clear();
        return claimed;
    }

    synchronized List<ShoppingItem> items(String group) {
        return List.copyOf(itemsByGroup.getOrDefault(group, List.of()));
    }

}
