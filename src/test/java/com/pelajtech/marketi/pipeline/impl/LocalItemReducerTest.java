package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemLog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.pelajtech.marketi.item.ItemHelpers.item;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalItemReducerTest {

    private static void appendAndCommit(LocalItemLog.LocalItemLogFactory factory, ShoppingItem... items) {
        ItemLog log = factory.create().orElseThrow();

        for (var item : items) {
            log.append(item);
        }

        log.commit();
    }

    @Test
    void reduceGroupRejectsNullGroupAndNullSink() {
        var reducer = LocalItemLog.factory().reducer();

        assertThrows(NullPointerException.class, () -> reducer.reduceGroup(null, _ -> {
        }));
        assertThrows(NullPointerException.class, () -> reducer.reduceGroup("bulmet", null));
    }

    @Test
    void reduceGroupDoesNotCallSinkWhenNoItemsExistForGroup() {
        var reducer = LocalItemLog.factory().reducer();
        var reduced = new ArrayList<ShoppingItem>();

        reducer.reduceGroup("mungon", reduced::add);

        assertEquals(List.of(), reduced);
    }

    @Test
    void reduceGroupEmitsItemWithHighestScoringNameInGroup() {
        var factory = LocalItemLog.factory();
        var cleanProduct = item("bulmet", "Qumësht natyral 1L", "viva", BigDecimal.TEN, BigDecimal.ONE);
        var promoProduct = item("bulmet", "Aksion qumësht 1L 0.99 euro", "other", BigDecimal.ONE, BigDecimal.TEN);

        appendAndCommit(factory, promoProduct, cleanProduct);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduceGroup("bulmet", reduced::add);

        assertEquals("Qumësht natyral 1L", reduced.getFirst().name());
    }

    @Test
    void reduceGroupConsidersAllItemsStoredForGroup() {
        var factory = LocalItemLog.factory();
        var firstItem = item("bulmet", "Aksion qumësht 1L 0.99 euro");
        var betterLaterItem = item("bulmet", "Qumësht natyral 1L");

        appendAndCommit(factory, firstItem, betterLaterItem);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduceGroup("bulmet", reduced::add);

        assertEquals("Qumësht natyral 1L", reduced.getFirst().name());
    }

    @Test
    void reduceGroupMergesMarketDataFromCandidates() {
        var factory = LocalItemLog.factory();
        var viva = item("bulmet", "Qumësht natyral 1L", "viva", new BigDecimal("10"), new BigDecimal("1.20"));
        var other = item("bulmet", "Aksion qumësht 1L 0.99 euro", "other", new BigDecimal("5"), new BigDecimal("0.99"));

        appendAndCommit(factory, viva, other);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduceGroup("bulmet", reduced::add);
        var item = reduced.getFirst();

        assertEquals(Set.of("viva", "other"), item.markets());
        assertEquals(new BigDecimal("10"), item.marketStockCount().get("viva"));
        assertEquals(new BigDecimal("5"), item.marketStockCount().get("other"));
        assertEquals(new BigDecimal("1.20"), item.marketPrices().get("viva").price());
        assertEquals(new BigDecimal("0.99"), item.marketPrices().get("other").price());
    }

    @Test
    void reduceGroupKeepsFirstMarketDataWhenCandidatesShareMarket() {
        var factory = LocalItemLog.factory();
        var first = item("bulmet", "Qumësht natyral 1L", "viva", new BigDecimal("10"), new BigDecimal("1.20"));
        var second = item("bulmet", "Qumësht natyral 1L", "viva", new BigDecimal("99"), new BigDecimal("9.99"));

        appendAndCommit(factory, first, second);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduceGroup("bulmet", reduced::add);
        var item = reduced.getFirst();

        assertEquals(new BigDecimal("10"), item.marketStockCount().get("viva"));
        assertEquals(new BigDecimal("1.20"), item.marketPrices().get("viva").price());
    }

    @Test
    void reduceGroupEmitsFirstItemWhenHighestScoresAreTied() {
        var factory = LocalItemLog.factory();
        var first = item("bulmet", "Qumësht natyral 1L");
        var second = item("bulmet", "Qumesht natyral 1L");

        appendAndCommit(factory, first, second);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduceGroup("bulmet", reduced::add);

        assertEquals(first.name(), reduced.getFirst().name());
    }

    @Test
    void differentGroupsAreReducedIndependently() {
        var factory = LocalItemLog.factory();
        appendAndCommit(factory,
                item("bulmet", "Qumësht natyral 1L"),
                item("furre", "Bukë integrale 500g")
        );

        var reduced = new ArrayList<ShoppingItem>();
        var reducer = factory.reducer();
        reducer.reduceGroup("bulmet", reduced::add);
        reducer.reduceGroup("furre", reduced::add);

        assertEquals(List.of("Qumësht natyral 1L", "Bukë integrale 500g"), reduced.stream()
                .map(ShoppingItem::name)
                .toList());
    }

    @Test
    void claimGroupsDelegatesToStoreAndClearsPendingGroups() {
        var factory = LocalItemLog.factory();
        var reducer = factory.reducer();

        appendAndCommit(factory, item("bulmet", "Qumësht natyral 1L"));

        assertEquals(Set.of("bulmet"), reducer.claimGroups());
        assertEquals(Set.of(), reducer.claimGroups());
    }

}
