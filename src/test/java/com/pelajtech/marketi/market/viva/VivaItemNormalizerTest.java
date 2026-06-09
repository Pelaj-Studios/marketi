package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.item.RawShoppingItem;
import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.utils.CountryFlagUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VivaItemNormalizerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final VivaItemNormalizer normalizer = new VivaItemNormalizer();

    private static VivaResponse.Product product(long id) throws IOException {
        return products().stream()
                .filter(product -> product.id() == id)
                .findAny()
                .orElseThrow();
    }

    private static List<VivaResponse.Product> products() throws IOException {
        try (var input = VivaItemNormalizerTest.class.getResourceAsStream(
                "/market/viva/product-loyalty-page-1.json"
        )) {
            var response = OBJECT_MAPPER.readValue(input, VivaResponse.class);
            return response.data().orElseThrow();
        }
    }

    @Test
    void normalizesDiscountedVivaProductFromRealJsonFixture() throws IOException {
        var product = product(882);
        var raw = Viva.rawShoppingItem(product).orElseThrow();
        var item = normalizer.normalize(raw).orElseThrow();

        assertEquals("feferona te djeges deluxe 720 g", item.group());
        assertEquals("Feferona te djeges Deluxe 720gr", item.name());
        assertEquals("3903671510267", item.barcode());
        assertEquals(CountryFlagUtils.toIso2(product.originFlag().orElseThrow()).orElseThrow(), item.countryOfOrigin());
        assertEquals(ShoppingItem.Unit.PIECE, item.unit());
        assertEquals(new ShoppingItem.Quantity(new BigDecimal("1.00"), BigDecimal.ONE), item.quantity());
        assertEquals(new BigDecimal("113.00000"), item.marketStockCount().get(Viva.MARKET_ID));
        assertEquals(new BigDecimal("2.09"), item.marketPrices().get(Viva.MARKET_ID).price());
        assertEquals(Optional.of(new BigDecimal("1.25")), item.marketPrices().get(Viva.MARKET_ID).salePrice());
    }

    @Test
    void normalizesRegularVivaProductFromRealJsonFixture() throws IOException {
        var product = product(878);
        var item = normalizer.normalize(Viva.rawShoppingItem(product).orElseThrow()).orElseThrow();

        assertEquals("sheqer i kafte italianno 900 g", item.group());
        assertEquals("Sheqer i kafte Italianno 900g", item.name());
        assertEquals("3903671510212", item.barcode());
        assertEquals(ShoppingItem.Unit.PIECE, item.unit());
        assertEquals(new ShoppingItem.Quantity(new BigDecimal("1.00"), BigDecimal.ONE), item.quantity());
        assertEquals(new BigDecimal("28.00000"), item.marketStockCount().get(Viva.MARKET_ID));
        assertEquals(new BigDecimal("2.75"), item.marketPrices().get(Viva.MARKET_ID).price());
        assertEquals(Optional.empty(), item.marketPrices().get(Viva.MARKET_ID).salePrice());
    }

    @Test
    void returnsEmptyForDifferentMarket() {
        var raw = new RawShoppingItem(
                "raw-id",
                "other",
                "Sheqer 1kg",
                "",
                "XK",
                "COPE",
                "barcode",
                LocalDateTime.now(),
                BigDecimal.ONE,
                Optional.empty(),
                BigDecimal.ONE,
                BigDecimal.ONE
        );

        assertTrue(normalizer.normalize(raw).isEmpty());
    }

}
