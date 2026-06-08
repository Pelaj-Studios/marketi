package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.pipeline.ItemLog;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalItemLogTest {

    private static ItemLog createLog(LocalItemLog.LocalItemLogFactory factory) {
        return factory.create().orElseThrow();
    }

    private static ShoppingItem item(String group, String name) {
        return new ShoppingItem(
                UUID.randomUUID(),
                group,
                name,
                Set.of("market"),
                Map.of("market", 1),
                Map.of(),
                "Kosovo",
                "barcode-" + name,
                ShoppingItem.Unit.PIECE,
                new ShoppingItem.Quantity(1, 1)
        );
    }

    @Test
    void committedItemsMakeTheirGroupsClaimable() {
        var factory = LocalItemLog.factory();
        var log = createLog(factory);

        log.append(item("dairy", "Milk"));
        log.append(item("dairy", "Yogurt"));
        log.append(item("bakery", "Bread"));
        log.commit();

        assertEquals(Set.of("dairy", "bakery"), factory.reducer().claim());
    }

    @Test
    void claimClearsPendingGroupsUntilMoreItemsAreCommitted() {
        var factory = LocalItemLog.factory();
        var reducer = factory.reducer();
        var log = createLog(factory);

        log.append(item("dairy", "Milk"));
        log.commit();

        assertEquals(Set.of("dairy"), reducer.claim());
        assertEquals(Set.of(), reducer.claim());

        log.append(item("dairy", "Yogurt"));
        log.commit();

        assertEquals(Set.of("dairy"), reducer.claim());
    }

    @Test
    void separateLogsFromTheSameFactoryShareReducerState() {
        var factory = LocalItemLog.factory();
        var firstLog = createLog(factory);
        var secondLog = createLog(factory);

        firstLog.append(item("dairy", "Milk"));
        firstLog.commit();

        secondLog.append(item("bakery", "Bread"));
        secondLog.commit();

        assertEquals(Set.of("dairy", "bakery"), factory.reducer().claim());
    }

    @Test
    void uncommittedItemsAreNotClaimable() {
        var factory = LocalItemLog.factory();
        var log = createLog(factory);

        log.append(item("dairy", "Milk"));

        assertEquals(Set.of(), factory.reducer().claim());
    }

    @Test
    void appendRejectsNullItemsAndNullGroups() {
        var log = createLog(LocalItemLog.factory());

        assertThrows(NullPointerException.class, () -> log.append(null));
        assertThrows(NullPointerException.class, () -> log.append(item(null, "Milk")));
    }

}
