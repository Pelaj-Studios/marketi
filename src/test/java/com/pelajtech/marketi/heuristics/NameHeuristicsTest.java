package com.pelajtech.marketi.heuristics;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameHeuristicsTest {

    @Test
    void cleanProductNameScoresHigh() {
        assertEquals(40, NameHeuristics.NAME_HEURISTIC.score("Qumesht natyral 1L"));
    }

    @Test
    void productWordsMatchAlbanianDiacriticsAfterNormalization() {
        assertEquals(40, NameHeuristics.NAME_HEURISTIC.score("Qumësht natyral 1L"));
    }

    @Test
    void promotionalPriceTextIsPenalized() {
        assertEquals(-9, NameHeuristics.NAME_HEURISTIC.score("Aksion qumesht 1L 0.99 euro"));
    }

    @Test
    void metadataTextPreventsCleanProductBonus() {
        assertEquals(28, NameHeuristics.NAME_HEURISTIC.score("Paketim qumesht 1L"));
    }

    @Test
    void allCapsNamesScoreLowerThanNormalNames() {
        var normalScore = NameHeuristics.NAME_HEURISTIC.score("Qumesht natyral 1L");
        var allCapsScore = NameHeuristics.NAME_HEURISTIC.score("QUMESHT NATYRAL 1L");

        assertTrue(allCapsScore < normalScore);
    }

    @Test
    void highestSelectsCleanestProductName() {
        assertEquals(
                Optional.of("Qumësht natyral 1L"),
                NameHeuristics.NAME_HEURISTIC.highest(List.of(
                        "Aksion qumesht 1L 0.99 euro",
                        "Qumësht natyral 1L",
                        "Paketim qumesht 1L"
                ))
        );
    }

}
