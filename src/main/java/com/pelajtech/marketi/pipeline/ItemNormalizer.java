package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.RawShoppingItem;
import com.pelajtech.marketi.item.ShoppingItem;

import java.util.Optional;

public interface ItemNormalizer {

    Optional<ShoppingItem> normalize(RawShoppingItem item);

    interface Factory {

        Optional<ItemNormalizer> forMarket(String market);

    }

}
