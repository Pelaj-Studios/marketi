package com.pelajtech.marketi.pipeline.impl;

import com.pelajtech.marketi.pipeline.ItemLog;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.pelajtech.marketi.item.ItemHelpers.item;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalItemLogTest {

    private static ItemLog createLog(LocalItemLog.LocalItemLogFactory factory) {
        return factory.create().orElseThrow();
    }

    @Test
    void committedItemsMakeTheirGroupsClaimable() {
        var factory = LocalItemLog.factory();
        var log = createLog(factory);

        log.append(item("bulmet", "Qumësht natyral 1L"));
        log.append(item("bulmet", "Kos natyral 400g"));
        log.append(item("furre", "Bukë integrale 500g"));
        log.commit();

        assertEquals(Set.of("bulmet", "furre"), factory.reducer().claim());
    }

    @Test
    void claimClearsPendingGroupsUntilMoreItemsAreCommitted() {
        var factory = LocalItemLog.factory();
        var reducer = factory.reducer();
        var log = createLog(factory);

        log.append(item("bulmet", "Qumësht natyral 1L"));
        log.commit();

        assertEquals(Set.of("bulmet"), reducer.claim());
        assertEquals(Set.of(), reducer.claim());

        log.append(item("bulmet", "Kos natyral 400g"));
        log.commit();

        assertEquals(Set.of("bulmet"), reducer.claim());
    }

    @Test
    void separateLogsFromTheSameFactoryShareReducerState() {
        var factory = LocalItemLog.factory();
        var firstLog = createLog(factory);
        var secondLog = createLog(factory);

        firstLog.append(item("bulmet", "Qumësht natyral 1L"));
        firstLog.commit();

        secondLog.append(item("furre", "Bukë integrale 500g"));
        secondLog.commit();

        assertEquals(Set.of("bulmet", "furre"), factory.reducer().claim());
    }

    @Test
    void uncommittedItemsAreNotClaimable() {
        var factory = LocalItemLog.factory();
        var log = createLog(factory);

        log.append(item("bulmet", "Qumësht natyral 1L"));

        assertEquals(Set.of(), factory.reducer().claim());
    }

    @Test
    void appendRejectsNullItemsAndNullGroups() {
        var log = createLog(LocalItemLog.factory());

        assertThrows(NullPointerException.class, () -> log.append(null));
        assertThrows(NullPointerException.class, () -> log.append(item(null, "Qumësht natyral 1L")));
    }

}
