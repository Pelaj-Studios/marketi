package com.pelajtech.marketi.market.viva;

import com.pelajtech.marketi.item.RawShoppingItem;
import com.pelajtech.marketi.log.Logging;
import com.pelajtech.marketi.pipeline.ItemDownloader;
import com.pelajtech.marketi.utils.CountryFlagUtils;
import com.pelajtech.marketi.utils.DateUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VivaItemDownloader implements ItemDownloader {


    private static final int DEFAULT_CONCURRENCY = 4;
    private static final int DEFAULT_PER_PAGE = 20;
    private static final int DEFAULT_MAX_PAGES = Integer.MAX_VALUE;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final int concurrency;
    private final int perPage;
    private final int maxPages;

    public VivaItemDownloader() {
        this(DEFAULT_CONCURRENCY, DEFAULT_PER_PAGE, DEFAULT_MAX_PAGES);
    }

    public VivaItemDownloader(int concurrency, int perPage, int maxPages) {

        if (concurrency <= 0) {
            throw new IllegalArgumentException("Concurrency must be positive.");
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per-page count must be positive.");
        }

        if (maxPages <= 0) {
            throw new IllegalArgumentException("Max pages must be positive.");
        }

        this.concurrency = concurrency;
        this.perPage = perPage;
        this.maxPages = maxPages;
    }

    @Override
    public void download(Consumer<RawShoppingItem> sink) {
        Objects.requireNonNull(sink);

        var nextPage = new AtomicInteger(1);
        var stopAt = new AtomicInteger(maxPages == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxPages + 1);
        var httpClient = HttpClient.newHttpClient();
        var executor = Executors.newFixedThreadPool(concurrency);
        var futures = new ArrayList<java.util.concurrent.Future<?>>();

        try {
            for (var i = 0; i < concurrency; i++) {
                futures.add(executor.submit(() -> downloadPages(httpClient, nextPage, stopAt, sink)));
            }

            for (var future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stopAt.accumulateAndGet(nextPage.get(), Math::min);
        } catch (ExecutionException e) {
            stopAt.accumulateAndGet(nextPage.get(), Math::min);
            Logging.LOG.error("Failed to download Viva items.", e);
        } finally {
            httpClient.close();
            executor.shutdownNow();
        }
    }

    private void downloadPages(
            HttpClient httpClient,
            AtomicInteger nextPage,
            AtomicInteger stopAt,
            Consumer<RawShoppingItem> sink
    ) {
        while (true) {
            var page = nextPage.getAndIncrement();
            if (page >= stopAt.get()) {
                return;
            }

            try {
                var items = downloadPage(httpClient, page);
                Logging.LOG.debug("Completed Viva page {} with {} items.", page, items.size());
                if (items.isEmpty()) {
                    stopAt.accumulateAndGet(page, Math::min);
                    return;
                }

                items.forEach(sink);
            } catch (Exception e) {
                stopAt.accumulateAndGet(page, Math::min);
                return;
            }
        }
    }

    private List<RawShoppingItem> downloadPage(
            HttpClient httpClient,
            int page
    ) throws InterruptedException {
        var request = HttpRequest
                .newBuilder(Viva.productLoyaltyUri(page, perPage))
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            Logging.LOG.error("Failed to download Viva page {}.", page, e);
            return List.of();
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Logging.LOG.error("Viva returned HTTP {} for page {}.", response.statusCode(), page);
            return List.of();
        }

        if (response.body().isBlank()) {
            Logging.LOG.error("Viva returned a blank response body for page {}.", page);
            return List.of();
        }

        try {
            return mapResponse(OBJECT_MAPPER.readValue(response.body(), VivaResponse.class));
        } catch (JacksonException | IllegalArgumentException e) {
            Logging.LOG.error("Failed to map Viva page {}.", page, e);
            return List.of();
        }
    }

    private List<RawShoppingItem> mapResponse(VivaResponse response) {
        var items = new ArrayList<RawShoppingItem>();
        var products = response.data();
        if (products.isEmpty()) {
            Logging.LOG.error("Viva response is missing data.");
            return List.of();
        }

        for (var product : products.orElseThrow()) {
            mapProduct(product).ifPresent(items::add);
        }

        return items;
    }

    private Optional<RawShoppingItem> mapProduct(VivaResponse.Product product) {
        if (product.basePrice().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing base_price.", product.id());
            return Optional.empty();
        }

        if (product.finalPrice().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing final_price.", product.id());
            return Optional.empty();
        }

        if (product.increment().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing increment.", product.id());
            return Optional.empty();
        }

        if (product.stock().isEmpty()) {
            Logging.LOG.error("Viva product {} is missing stock.", product.id());
            return Optional.empty();
        }

        var basePrice = product.basePrice().orElseThrow();
        var finalPrice = product.finalPrice().orElseThrow();
        var salePrice = finalPrice.compareTo(basePrice) < 0 ? Optional.of(finalPrice) : Optional.<BigDecimal>empty();
        var quantity = parseBigDecimal(product.id(), "increment", product.increment().orElse(""));
        var stock = parseBigDecimal(product.id(), "stock", product.stock().orElse(""));

        if (quantity.isEmpty() || stock.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new RawShoppingItem(
                String.valueOf(product.id()),
                Viva.MARKET_ID,
                product.name().orElse(""),
                product.description().orElse(""),
                product.originFlag().flatMap(CountryFlagUtils::toIso2).orElse(""),
                product.unitOfMeasure().orElse(""),
                product.barcode().orElse(""),
                LocalDateTime.now(DateUtils.CLOCK),
                basePrice,
                salePrice,
                quantity.orElseThrow(),
                stock.orElseThrow()
        ));
    }

    private Optional<BigDecimal> parseBigDecimal(long productId, String field, String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            Logging.LOG.error("Viva product {} has invalid {} value {}.", productId, field, value);
            return Optional.empty();
        }
    }

}
