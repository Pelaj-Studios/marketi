package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.utils.CountryFlagUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryFlagUtilsTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            textBlock = """
                    🇽🇰 | XK
                    🇲🇰 | MK
                    🇦🇱 | AL
                    🇪🇬 | EG
                    """
    )
    void convertsFlagEmojiToIso2CountryCode(String flag, String expected) {
        assertEquals(Optional.of(expected), CountryFlagUtils.toIso2(flag));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "XK", "🇽", "🇽🇰🇦🇱", "x"})
    void returnsEmptyStringForMalformedInput(String flag) {
        assertEquals(Optional.empty(), CountryFlagUtils.toIso2(flag));
    }

}
