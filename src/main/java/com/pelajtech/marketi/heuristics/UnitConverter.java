package com.pelajtech.marketi.heuristics;

import com.pelajtech.marketi.item.ShoppingItem;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class UnitConverter {

    private static final Map<String, UnitMapping> UNITS = Map.ofEntries(
            Map.entry("l", litre("1")),
            Map.entry("lt", litre("1")),
            Map.entry("ltr", litre("1")),
            Map.entry("liter", litre("1")),
            Map.entry("liters", litre("1")),
            Map.entry("litre", litre("1")),
            Map.entry("litres", litre("1")),
            Map.entry("litra", litre("1")),
            Map.entry("literat", litre("1")),
            Map.entry("ml", litre("0.001")),
            Map.entry("milliliter", litre("0.001")),
            Map.entry("milliliters", litre("0.001")),
            Map.entry("millilitre", litre("0.001")),
            Map.entry("millilitres", litre("0.001")),
            Map.entry("cl", litre("0.01")),
            Map.entry("centiliter", litre("0.01")),
            Map.entry("centiliters", litre("0.01")),
            Map.entry("centilitre", litre("0.01")),
            Map.entry("centilitres", litre("0.01")),

            Map.entry("g", gram("1")),
            Map.entry("gr", gram("1")),
            Map.entry("gram", gram("1")),
            Map.entry("grams", gram("1")),
            Map.entry("gramme", gram("1")),
            Map.entry("grammes", gram("1")),
            Map.entry("grama", gram("1")),
            Map.entry("kg", gram("1000")),
            Map.entry("kgr", gram("1000")),
            Map.entry("kilogram", gram("1000")),
            Map.entry("kilograms", gram("1000")),
            Map.entry("kilogramme", gram("1000")),
            Map.entry("kilogrammes", gram("1000")),
            Map.entry("kilograma", gram("1000")),
            Map.entry("mg", gram("0.001")),
            Map.entry("milligram", gram("0.001")),
            Map.entry("milligrams", gram("0.001")),
            Map.entry("milligramme", gram("0.001")),
            Map.entry("milligrammes", gram("0.001")),

            Map.entry("cope", piece()),
            Map.entry("cop", piece()),
            Map.entry("kom", piece()),
            Map.entry("pcs", piece()),
            Map.entry("pc", piece()),
            Map.entry("piece", piece()),
            Map.entry("pieces", piece()),
            Map.entry("pako", piece()),
            Map.entry("pake", piece()),
            Map.entry("pak", piece()),
            Map.entry("pack", piece()),
            Map.entry("packs", piece()),
            Map.entry("packet", piece()),
            Map.entry("packets", piece()),
            Map.entry("qese", piece()),
            Map.entry("shishe", piece()),
            Map.entry("kanace", piece()),
            Map.entry("tablet", piece()),
            Map.entry("tableta", piece()),
            Map.entry("tablete", piece())
    );

    private UnitConverter() {
    }

    public static Optional<ShoppingItem.Unit> unit(String value) {
        return mapping(value).map(UnitMapping::unit);
    }

    public static Optional<String> canonicalToken(String value) {
        return mapping(value).map(UnitMapping::canonicalToken);
    }

    public static Optional<BigDecimal> factorToBaseUnit(String value) {
        return mapping(value).map(UnitMapping::factorToBaseUnit);
    }

    public static Optional<ConvertedQuantity> convert(String quantity, String unit) {
        return mapping(unit).map(mapping -> new ConvertedQuantity(
                new BigDecimal(normalizeQuantity(quantity)).multiply(mapping.factorToBaseUnit()),
                mapping.unit(),
                mapping.canonicalToken()
        ));
    }

    private static Optional<UnitMapping> mapping(String value) {
        return Optional.ofNullable(UNITS.get(normalize(value)));
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace('ç', 'c')
                .replace('ë', 'e')
                .replaceAll("[^\\p{L}\\p{N}]", "")
                .trim();
    }

    private static String normalizeQuantity(String quantity) {
        return quantity.replace(',', '.');
    }

    private static UnitMapping litre(String factorToBaseUnit) {
        return new UnitMapping(ShoppingItem.Unit.LITRE, "l", new BigDecimal(factorToBaseUnit));
    }

    private static UnitMapping gram(String factorToBaseUnit) {
        return new UnitMapping(ShoppingItem.Unit.GRAM, "g", new BigDecimal(factorToBaseUnit));
    }

    private static UnitMapping piece() {
        return new UnitMapping(ShoppingItem.Unit.PIECE, "piece", BigDecimal.ONE);
    }

    private record UnitMapping(ShoppingItem.Unit unit, String canonicalToken, BigDecimal factorToBaseUnit) {

    }

    public record ConvertedQuantity(BigDecimal quantity, ShoppingItem.Unit unit, String canonicalToken) {

    }

}
