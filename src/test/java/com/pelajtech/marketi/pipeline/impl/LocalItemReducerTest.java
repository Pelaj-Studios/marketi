package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemLog;
import org.junit.jupiter.api.Test;

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
    void reduceRejectsNullKeysAndNullSink() {
        var reducer = LocalItemLog.factory().reducer();

        assertThrows(NullPointerException.class, () -> reducer.reduce(null, _ -> {
        }));
        assertThrows(NullPointerException.class, () -> reducer.reduce(Set.of("bulmet"), null));
    }

    @Test
    void reduceDoesNotCallSinkWhenNoItemsExistForKeys() {
        var reducer = LocalItemLog.factory().reducer();
        var reduced = new ArrayList<ShoppingItem>();

        reducer.reduce(Set.of("mungon"), reduced::add);

        assertEquals(List.of(), reduced);
    }

    @Test
    void reduceEmitsItemWithHighestScoringNameAcrossGroups() {
        var factory = LocalItemLog.factory();
        var cleanProduct = item("bulmet", "Qumësht natyral 1L");
        var promoProduct = item("oferta", "Aksion qumësht 1L 0.99 euro");

        appendAndCommit(factory, promoProduct, cleanProduct);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduce(Set.of("oferta", "bulmet"), reduced::add);

        assertEquals(List.of(cleanProduct), reduced);
    }

    @Test
    void reduceOnlyConsidersFirstItemStoredForEachGroup() {
        var factory = LocalItemLog.factory();
        var firstItem = item("bulmet", "Aksion qumësht 1L 0.99 euro");
        var betterLaterItem = item("bulmet", "Qumësht natyral 1L");

        appendAndCommit(factory, firstItem, betterLaterItem);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduce(Set.of("bulmet"), reduced::add);

        assertEquals(List.of(firstItem), reduced);
    }

    @Test
    void reduceEmitsFirstItemWhenHighestScoresAreTied() {
        var factory = LocalItemLog.factory();
        var first = item("bulmet", "Qumësht natyral 1L");
        var second = item("bulmet-alt", "Qumesht natyral 1L");

        appendAndCommit(factory, first, second);

        var reduced = new ArrayList<ShoppingItem>();
        factory.reducer().reduce(List.of("bulmet", "bulmet-alt"), reduced::add);

        assertEquals(List.of(first), reduced);
    }

    @Test
    void claimDelegatesToStoreAndClearsPendingGroups() {
        var factory = LocalItemLog.factory();
        var reducer = factory.reducer();

        appendAndCommit(factory, item("bulmet", "Qumësht natyral 1L"));

        assertEquals(Set.of("bulmet"), reducer.claim());
        assertEquals(Set.of(), reducer.claim());
    }

}
