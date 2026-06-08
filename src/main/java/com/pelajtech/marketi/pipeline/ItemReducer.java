package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.ShoppingItem;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface ItemReducer {

    void reduce(Collection<String> keys, Consumer<ShoppingItem> sink);

    Set<String> claim();
}
