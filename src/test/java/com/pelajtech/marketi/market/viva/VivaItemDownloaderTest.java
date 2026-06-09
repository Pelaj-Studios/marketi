package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.item.RawShoppingItem;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VivaItemDownloaderTest {

    private static final Pattern ISO2 = Pattern.compile("[A-Z]{2}");

    @Test
    void downloadsRealVivaProductsAndMapsThemToRawShoppingItems() {
        var downloader = new VivaItemDownloader(4, 100, 2);
        var items = new CopyOnWriteArrayList<RawShoppingItem>();

        downloader.download(items::add);

        assertFalse(items.isEmpty());
        assertTrue(items.stream().allMatch(item -> Viva.MARKET_ID.equals(item.marketId())));
        assertTrue(items.stream().noneMatch(item -> item.rawId().isBlank()));
        assertTrue(items.stream().noneMatch(item -> item.name().isBlank()));
        assertTrue(items.stream().noneMatch(item -> item.stockUnit().isBlank()));
        assertTrue(items.stream().allMatch(item -> item.quantity() > 0));
        assertTrue(items.stream().allMatch(item -> item.stock() >= 0));
        assertTrue(items.stream().allMatch(item -> item.countryOfOrigin().isEmpty()
                || ISO2.matcher(item.countryOfOrigin()).matches()));
        assertTrue(items.stream().allMatch(item -> item.salePrice().isEmpty()
                || item.salePrice().orElseThrow().compareTo(item.price()) < 0));
        items.stream()
                .filter(item -> item.countryOfOrigin().equals("XK"))
                .findAny()
                .orElseThrow();
    }

}
