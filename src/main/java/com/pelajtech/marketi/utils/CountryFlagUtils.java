package com.pelajtech.marketi.utils;

import java.util.Optional;

public final class CountryFlagUtils {

    private static final int REGIONAL_INDICATOR_A = 0x1F1E6;
    private static final int REGIONAL_INDICATOR_Z = 0x1F1FF;

    private CountryFlagUtils() {
    }

    public static Optional<String> toIso2(String value) {
        if (value.isBlank()) {
            return Optional.empty();
        }

        var codePoints = value.codePoints().toArray();
        if (codePoints.length != 2) {
            return Optional.empty();
        }

        var first = toCountryLetter(codePoints[0]);
        var second = toCountryLetter(codePoints[1]);
        if (first == 0 || second == 0) {
            return Optional.empty();
        }

        return Optional.of(String.valueOf(new char[]{first, second}));
    }

    private static char toCountryLetter(int codePoint) {
        if (codePoint < REGIONAL_INDICATOR_A || codePoint > REGIONAL_INDICATOR_Z) {
            return 0;
        }

        return (char) ('A' + codePoint - REGIONAL_INDICATOR_A);
    }

}
