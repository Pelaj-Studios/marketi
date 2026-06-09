package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.item.RawShoppingItem;
import com.pelajtech.marketi.log.Logging;
import com.pelajtech.marketi.utils.CountryFlagUtils;
import com.pelajtech.marketi.utils.DateUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Viva {

    public static final String BASE_URL = "https://online.vivafresh.shop/";
    public static final String MARKET_ID = "viva";
    public static final String PRODUCT_LOYALTY_URL = BASE_URL
            + "lib/config/proxy.php?endpoint=shop/product-loyalty?organization_id=9"
            + "&page=%d&per_page=%d&filter%%5Btags%%5D=produktet-tona";

    private Viva() {
    }

    public static URI productLoyaltyUri(int page, int perPage) {
        return URI.create(PRODUCT_LOYALTY_URL.formatted(page, perPage));
    }

    public static List<RawShoppingItem> rawShoppingItems(VivaResponse response) {
        var items = new ArrayList<RawShoppingItem>();
        var products = response.data();
        if (products.isEmpty()) {
            Logging.LOG.error("Viva response is missing data.");
            return List.of();
        }

        for (var product : products.orElseThrow()) {
            rawShoppingItem(product).ifPresent(items::add);
        }

        return items;
    }

    public static Optional<RawShoppingItem> rawShoppingItem(VivaResponse.Product product) {
        if (product.basePrice().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing base_price.", product.id());
            return Optional.empty();
        }

        if (product.finalPrice().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing final_price.", product.id());
            return Optional.empty();
        }

        if (product.increment().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing increment.", product.id());
            return Optional.empty();
        }

        if (product.stock().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing stock.", product.id());
            return Optional.empty();
        }

        var basePrice = product.basePrice().orElseThrow();
        var finalPrice = product.finalPrice().orElseThrow();
        var salePrice = finalPrice.compareTo(basePrice) < 0 ? Optional.of(finalPrice) : Optional.<BigDecimal>empty();
        var quantity = parseBigDecimal(product.id(), "increment", product.increment().orElse(""));
        var stock = parseBigDecimal(product.id(), "stock", product.stock().orElse(""));

        if (quantity.isEmpty() || stock.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new RawShoppingItem(
                String.valueOf(product.id()),
                MARKET_ID,
                product.name().orElse(""),
                product.description().orElse(""),
                product.originFlag().flatMap(CountryFlagUtils::toIso2).orElse(""),
                product.unitOfMeasure().orElse(""),
                product.barcode().orElse(""),
                LocalDateTime.now(DateUtils.CLOCK),
                basePrice,
                salePrice,
                quantity.orElseThrow(),
                stock.orElseThrow()
        ));
    }

    private static Optional<BigDecimal> parseBigDecimal(long productId, String field, String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            Logging.LOG.error("Viva product {} has invalid {} value {}.", productId, field, value);
            return Optional.empty();
        }
    }

}
