package com.pelajtech.marketi.market.viva;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public record VivaResponse(Optional<List<Product>> data) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Product(
            long id,
            Optional<String> name,
            Optional<String> description,
            Optional<String> barcode,
            Optional<String> unitOfMeasure,
            Optional<BigDecimal> basePrice,
            Optional<BigDecimal> finalPrice,
            Optional<String> increment,
            Optional<String> stock,
            Optional<String> originFlag
    ) {
    }
}
