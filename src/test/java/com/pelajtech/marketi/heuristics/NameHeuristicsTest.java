package com.pelajtech.marketi.heuristics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    1l qumesht natyral | qumesht natyral 1 l
                    1 l qumesht natyral | qumesht natyral 1 l
                    Qumësht 1L natyral | qumesht natyral 1 l
                    qumesht natyral 1L | qumesht natyral 1 l
                    QUMESHT NATYRAL 1 LT | qumesht natyral 1 l
                    Qumësht natyral 1 liter | qumesht natyral 1 l
                    Vaj Ulliri 750ML Extra i Virgjer | vaj ulliri extra i virgjer 0.75 l
                    Coca-Cola Zero 0,5L | coca cola zero 0.5 l
                    Coca Cola Zero 0.5l | coca cola zero 0.5 l
                    Miell Gruri 5kg | miell gruri 5000 g
                    Kafe e bluar 250gr | kafe e bluar 250 g
                    Sheqer 1 KG | sheqer 1000 g
                    Kos 400 g Natyral | kos natyral 400 g
                    Ujë Rugove 1.5L | uje rugove 1.5 l
                    Djath i bardhë 800g + 200g gratis | djath i bardhe gratis 800 g 200 g
                    Biskota 3 pako | biskota 3 piece
                    Çokollatë 6 pcs | cokollate 6 piece
                    Kanaçe Cola 330ml | kanace cola 0.33 l
                    """
    )
    void unitsAtEndCanonicalizesMarketNameVariants(String name, String expected) {
        assertEquals(expected, NameHeuristics.unitsAtEnd(name));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    Qumësht natyral | qumesht natyral
                    Domate të freskëta | domate te fresketa
                    bukë integrale | buke integrale
                    """
    )
    void unitsAtEndKeepsNamesWithoutUnitsNormalized(String name, String expected) {
        assertEquals(expected, NameHeuristics.unitsAtEnd(name));
    }

}
