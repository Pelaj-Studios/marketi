package com.pelajtech.marketi.heuristics;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/// Mostly LLM-generated.
public final class NameHeuristics {

    public static final HeuristicScorer<String> NAME_HEURISTIC = HeuristicScorer.<String>builder()
            // Positive signals
            .add(NameHeuristics::hasWord, 5)
            .add(NameHeuristics::hasReasonableLength, 10)
            .add(NameHeuristics::hasReasonableTokenCount, 10)
            .add(NameHeuristics::hasProductWord, 15)
            .add(NameHeuristics::hasDescriptorWord, 4)
            .add(NameHeuristics::hasExactlyOneUnit, 6)
            .add(NameHeuristics::endsWithUnit, 5)
            .add(NameHeuristics::looksLikeCleanProductName, 15)

            // Negative signals
            .add(NameHeuristics::startsWithQuantity, -4)
            .add(NameHeuristics::hasPromoWord, -20)
            .add(NameHeuristics::hasCurrencyPrice, -25)
            .add(NameHeuristics::hasUnitPrice, -20)
            .add(NameHeuristics::hasMetadata, -8)
            .add(NameHeuristics::hasMultipleUnits, -8)
            .add(NameHeuristics::isAllCaps, -4)
            .build();

    private static final Pattern WORD = Pattern.compile("\\p{L}{3,}");
    private static final Pattern UNIT = Pattern.compile(
            "(?iu)\\b(\\d+(?:[,.]\\d+)?)\\s?(kilogrammes?|kilograms?|kilograma|kgr|kg|milligrammes?|milligrams?|mg|grammes?|grams?|grama|gr|g|millilitres?|milliliters?|ml|centilitres?|centiliters?|cl|litres?|liters?|literat|litra|ltr|lt|l|cop[eë]?|kom|pcs?|pieces?|packs?|packets?|pak[oë]?|pako|qese|shishe|kana[cç]e|tablet[aë]?)\\b"
    );
    private static final Pattern ENDS_WITH_UNIT = Pattern.compile(
            "(?iu).*\\b\\d+([,.]\\d+)?\\s?(kg|g|gr|mg|l|lt|ml|cl|cop[eë]?|kom|pak[oë]?|pako|qese|shishe|kana[cç]e|tablet[aë]?)\\s*$"
    );
    private static final Pattern STARTS_WITH_QUANTITY = Pattern.compile(
            "(?iu)^\\s*\\d+([,.]\\d+)?\\s?(kg|g|gr|mg|l|lt|ml|cl|cop[eë]?|kom|pak[oë]?|pako|qese|shishe|kana[cç]e|x|×)\\b.*"
    );
    private static final Pattern PROMO = Pattern.compile(
            "(?iu)\\b(aksion|ofert[eë]?|zbritje|gratis|falas|super\\s*çmim|super\\s*cmim|lir[eë]|vet[eë]m|speciale?)\\b"
    );
    private static final Pattern CURRENCY_PRICE = Pattern.compile(
            "(?iu).*\\b\\d+([,.]\\d{1,2})?\\s?(€|eur|euro|lek[eë]?|den|mkd)\\b.*"
    );
    private static final Pattern UNIT_PRICE = Pattern.compile(
            "(?iu).*\\b\\d+([,.]\\d{1,2})?\\s?(€|eur|euro)?\\s*/\\s?(kg|g|gr|l|lt|ml)\\b.*"
    );
    private static final Pattern METADATA = Pattern.compile(
            "(?iu)\\b(paketim|paketimi|ambalazh|ambalazhi|artikull|barkod|barcode|ean|sku)\\b"
    );
    private static final Set<String> PRODUCT_WORDS = Set.of(
            "qumesht", "kos", "djath", "gjalp", "ajke",
            "buke", "miell", "oriz", "makarona", "spageti",
            "vaj", "sheqer", "kripe", "kafe", "caj",
            "uje", "leng", "cola", "cokollate", "biskota",
            "mish", "pule", "suxhuk", "sallam",
            "domate", "kastravec", "patate", "qepe",
            "molle", "banane", "portokall"
    );
    private static final Set<String> DESCRIPTOR_WORDS = Set.of(
            "natyral", "bio", "organik", "integral",
            "fresket", "tymosur", "bluar",
            "bardhe", "zi", "kuq",
            "zero", "light", "classic", "origjinal"
    );

    private NameHeuristics() {
    }

    private static boolean hasWord(String name) {
        return WORD.matcher(name).find();
    }

    private static boolean hasReasonableLength(String name) {
        int length = name.trim().length();
        return length >= 8 && length <= 70;
    }

    private static boolean hasReasonableTokenCount(String name) {
        int count = tokenCount(name);
        return count >= 2 && count <= 6;
    }

    private static boolean hasExactlyOneUnit(String name) {
        return unitCount(name) == 1;
    }

    private static boolean hasMultipleUnits(String name) {
        return unitCount(name) > 1;
    }

    private static boolean endsWithUnit(String name) {
        return ENDS_WITH_UNIT.matcher(name).matches();
    }

    private static boolean startsWithQuantity(String name) {
        return STARTS_WITH_QUANTITY.matcher(name).matches();
    }

    private static boolean hasPromoWord(String name) {
        return PROMO.matcher(name).find();
    }

    private static boolean hasCurrencyPrice(String name) {
        return CURRENCY_PRICE.matcher(name).matches();
    }

    private static boolean hasUnitPrice(String name) {
        return UNIT_PRICE.matcher(name).matches();
    }

    private static boolean hasMetadata(String name) {
        return METADATA.matcher(name).find();
    }

    private static boolean hasProductWord(String name) {
        return containsAnyToken(name, PRODUCT_WORDS);
    }

    private static boolean hasDescriptorWord(String name) {
        return containsAnyToken(name, DESCRIPTOR_WORDS);
    }

    private static boolean looksLikeCleanProductName(String name) {
        return hasReasonableTokenCount(name)
                && hasProductWord(name)
                && !hasPromoWord(name)
                && !hasCurrencyPrice(name)
                && !hasUnitPrice(name)
                && !hasMetadata(name);
    }

    private static boolean isAllCaps(String name) {
        String trimmed = name.trim();

        return trimmed.length() > 12
                && trimmed.equals(trimmed.toUpperCase(Locale.ROOT))
                && !trimmed.equals(trimmed.toLowerCase(Locale.ROOT));
    }

    private static boolean containsAnyToken(String name, Set<String> tokens) {
        return Arrays.stream(normalize(name).split("\\s+"))
                .anyMatch(tokens::contains);
    }

    private static long unitCount(String name) {
        return UNIT.matcher(name).results().count();
    }

    private static int tokenCount(String name) {
        String trimmed = name.trim();

        if (trimmed.isEmpty()) {
            return 0;
        }

        return trimmed.split("\\s+").length;
    }

    static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replace('ç', 'c')
                .replace('ë', 'e')
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static String unitsAtEnd(String name) {
        var matcher = UNIT.matcher(name);
        var units = new StringBuilder();
        var withoutUnits = new StringBuilder();

        while (matcher.find()) {
            if (!units.isEmpty()) {
                units.append(' ');
            }

            var converted = UnitConverter.convert(matcher.group(1), matcher.group(2))
                    .orElseThrow();

            units.append(formatQuantity(converted.quantity()))
                    .append(' ')
                    .append(converted.canonicalToken());
            matcher.appendReplacement(withoutUnits, " ");
        }

        matcher.appendTail(withoutUnits);

        var baseName = normalize(withoutUnits.toString());

        if (units.isEmpty()) {
            return baseName;
        }

        if (baseName.isEmpty()) {
            return units.toString();
        }

        return baseName + " " + units;
    }

    private static String formatQuantity(java.math.BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }
}
