package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.log.Logging;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Pipeline {

    private final Set<ItemDownloader> downloaders;
    private final ItemNormalizer.Factory normalizerFactory;
    private final ItemLog.Factory logFactory;
    private final ItemReducer reducer;
    private final Consumer<ShoppingItem> sink;

    private long reducerIntervalInSeconds = 1;

    private ScheduledExecutorService reducerExecutor;
    private ExecutorService downloaderExecutor;

    public Pipeline(
            Set<ItemDownloader> downloaders,
            ItemNormalizer.Factory normalizerFactory,
            ItemLog.Factory logFactory,
            ItemReducer reducer,
            Consumer<ShoppingItem> sink
    ) {
        this.downloaders = downloaders;
        this.normalizerFactory = normalizerFactory;
        this.logFactory = logFactory;
        this.reducer = reducer;
        this.sink = sink;
    }

    public long reducerIntervalInSeconds() {
        return reducerIntervalInSeconds;
    }

    public Pipeline setReducerIntervalInSeconds(long reducerIntervalInSeconds) {
        if (reducerIntervalInSeconds <= 0) {
            throw new IllegalArgumentException("Reducer interval must be positive.");
        }

        this.reducerIntervalInSeconds = reducerIntervalInSeconds;
        return this;
    }

    public synchronized void start() {
        if (isRunning()) {
            throw new IllegalStateException("Pipeline is already running.");
        }

        reducerExecutor = Executors.newSingleThreadScheduledExecutor();
        downloaderExecutor = Executors.newFixedThreadPool(Math.max(1, downloaders.size()));

        startReducer();
        startDownloaders();
    }

    private void startReducer() {
        reducerExecutor.scheduleWithFixedDelay(
                this::pollAndRunReduce,
                reducerIntervalInSeconds,
                reducerIntervalInSeconds,
                TimeUnit.SECONDS
        );
    }

    private void pollAndRunReduce() {
        try {
            var polled = reducer.claim();
            if (!polled.isEmpty()) {
                reducer.reduce(polled, sink);
            }
        } catch (Exception e) {
            Logging.LOG.error("Failed to claim reducer keys.", e);
        }
    }

    private void startDownloaders() {
        for (var downloader : downloaders) {
            downloaderExecutor.submit(() -> startDownload(downloader));
        }
    }

    private void startDownload(ItemDownloader downloader) {
        try {
            var logger = logFactory.create()
                    .orElseThrow(() -> new IllegalStateException("Was unable to create log."));

            downloader.download(raw -> normalizerFactory
                    .forMarket(raw.marketId())
                    .flatMap(normalizer -> normalizer.normalize(raw))
                    .ifPresent(logger::append)
            );

            logger.commit();
        } catch (Exception e) {
            Logging.LOG.error("Something went wrong when starting a downloader.", e);
        }
    }

    public synchronized void shutdown() {
        if (downloaderExecutor != null) {
            downloaderExecutor.shutdown();
        }

        if (reducerExecutor != null) {
            reducerExecutor.shutdown();
        }
    }

    public synchronized void shutdownNow() {
        if (downloaderExecutor != null) {
            downloaderExecutor.shutdownNow();
        }

        if (reducerExecutor != null) {
            reducerExecutor.shutdownNow();
        }
    }

    private boolean isRunning() {
        return reducerExecutor != null
                && !reducerExecutor.isShutdown();
    }
}