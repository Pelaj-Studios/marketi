package com.pelajtech.marketi.heuristics;

import com.pelajtech.marketi.item.ShoppingItem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitConverterTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    l | LITRE | l | 1
                    lt | LITRE | l | 1
                    LTR | LITRE | l | 1
                    liter | LITRE | l | 1
                    litres | LITRE | l | 1
                    litra | LITRE | l | 1
                    ml | LITRE | l | 0.001
                    milliliters | LITRE | l | 0.001
                    cl | LITRE | l | 0.01
                    kg | GRAM | g | 1000
                    kgr | GRAM | g | 1000
                    kilogram | GRAM | g | 1000
                    gr | GRAM | g | 1
                    gram | GRAM | g | 1
                    mg | GRAM | g | 0.001
                    copë | PIECE | piece | 1
                    kom | PIECE | piece | 1
                    pcs | PIECE | piece | 1
                    pako | PIECE | piece | 1
                    qese | PIECE | piece | 1
                    shishe | PIECE | piece | 1
                    kanaçe | PIECE | piece | 1
                    tabletë | PIECE | piece | 1
                    """
    )
    void convertsKnownMarketAliasesToShoppingItemUnits(
            String alias,
            ShoppingItem.Unit unit,
            String canonicalToken,
            BigDecimal factorToBaseUnit
    ) {
        assertEquals(Optional.of(unit), UnitConverter.unit(alias));
        assertEquals(Optional.of(canonicalToken), UnitConverter.canonicalToken(alias));
        assertEquals(Optional.of(factorToBaseUnit), UnitConverter.factorToBaseUnit(alias));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    1 | lt | 1 | LITRE | l
                    750 | ml | 0.750 | LITRE | l
                    0,5 | l | 0.5 | LITRE | l
                    5 | kg | 5000 | GRAM | g
                    250 | gr | 250 | GRAM | g
                    25 | mg | 0.025 | GRAM | g
                    3 | pako | 3 | PIECE | piece
                    """
    )
    void convertsQuantitiesToShoppingItemBaseUnits(
            String quantity,
            String alias,
            BigDecimal expectedQuantity,
            ShoppingItem.Unit expectedUnit,
            String expectedToken
    ) {
        assertEquals(
                Optional.of(new UnitConverter.ConvertedQuantity(expectedQuantity, expectedUnit, expectedToken)),
                UnitConverter.convert(quantity, alias)
        );
    }

    @ParameterizedTest
    @CsvSource({"euro", "barcode", "xyz"})
    void returnsEmptyForUnknownUnits(String value) {
        assertEquals(Optional.empty(), UnitConverter.unit(value));
        assertEquals(Optional.empty(), UnitConverter.canonicalToken(value));
    }

}
