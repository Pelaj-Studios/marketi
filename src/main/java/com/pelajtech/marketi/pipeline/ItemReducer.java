package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.ShoppingItem;

import java.util.Set;
import java.util.function.Consumer;

public interface ItemReducer {

    void reduceGroup(String group, Consumer<ShoppingItem> sink);

    Set<String> claimGroups();
}
