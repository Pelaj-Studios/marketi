package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.heuristics.GroupIdHeuristics;
import com.pelajtech.marketi.heuristics.UnitConverter;
import com.pelajtech.marketi.item.RawShoppingItem;
import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.log.Logging;
import com.pelajtech.marketi.pipeline.ItemNormalizer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VivaItemNormalizer implements ItemNormalizer {

    @Override
    public Optional<ShoppingItem> normalize(RawShoppingItem item) {
        if (!Viva.MARKET_ID.equals(item.marketId())) {
            Logging.LOG.error("Viva normalizer received item from market {}.", item.marketId());
            return Optional.empty();
        }

        var converted = UnitConverter.convert(item.quantity().toPlainString(), item.stockUnit());
        if (converted.isEmpty()) {
            Logging.LOG.error("Could not normalize Viva item {} with unit {}.", item.rawId(), item.stockUnit());
            return Optional.empty();
        }

        var quantity = converted.orElseThrow();

        return Optional.of(new ShoppingItem(
                UUID.randomUUID(),
                GroupIdHeuristics.defaultGroupIdGenerator(item),
                item.name(),
                Set.of(Viva.MARKET_ID),
                Map.of(Viva.MARKET_ID, item.stock()),
                Map.of(Viva.MARKET_ID, new ShoppingItem.Price(Viva.MARKET_ID, item.price(), item.salePrice())),
                item.countryOfOrigin(),
                item.barcode(),
                quantity.unit(),
                new ShoppingItem.Quantity(quantity.quantity(), BigDecimal.ONE)
        ));
    }

}
