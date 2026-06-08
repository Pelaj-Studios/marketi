package com.pelajtech.marketi.heuristics;

import com.pelajtech.marketi.item.RawShoppingItem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupIdHeuristicsTest {

    private static RawShoppingItem item(String name) {
        return new RawShoppingItem(
                "raw-id",
                "market-id",
                name,
                "",
                "",
                "",
                "",
                LocalDateTime.now(),
                BigDecimal.ONE,
                null,
                1,
                1
        );
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    Qumësht 1L natyral | qumesht natyral 1 l
                    1 L Qumësht natyral | qumesht natyral 1 l
                    Qumesht Natyral 1 lt | qumesht natyral 1 l
                    Qumesht Natyral 1 liter | qumesht natyral 1 l
                    Coca-Cola Zero 0,5L | coca cola zero 0.5 l
                    Coca Cola Zero 0.5l | coca cola zero 0.5 l
                    Ujë Rugove 1.5L | uje rugove 1.5 l
                    Vaj Ulliri Extra i Virgjer 750ML | vaj ulliri extra i virgjer 0.75 l
                    Kos Natyral 400 g | kos natyral 400 g
                    Kafe e bluar 250gr | kafe e bluar 250 g
                    Biskota familjare 3 pako | biskota familjare 3 piece
                    """
    )
    void defaultGroupIdGeneratorCanonicalizesMarketNameVariants(String name, String expected) {
        assertEquals(expected, GroupIdHeuristics.defaultGroupIdGenerator(item(name)));
    }

}
