package com.pelajtech.marketi.pipeline;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.pelajtech.marketi.item.ShoppingItem;
import com.pelajtech.marketi.log.Logging;
import com.pelajtech.marketi.market.viva.Viva;
import com.pelajtech.marketi.market.viva.VivaItemDownloader;
import com.pelajtech.marketi.market.viva.VivaItemNormalizer;
import com.pelajtech.marketi.pipeline.impl.LocalItemLog;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private static Pipeline pipelineWithNoDownloaders() {
        return new Pipeline(
                Set.of(),
                _ -> Optional.empty(),
                LocalItemLog.factory(),
                new ItemReducer() {
                    @Override
                    public void reduceGroup(String group, java.util.function.Consumer<ShoppingItem> sink) {
                    }

                    @Override
                    public Set<String> claimGroups() {
                        return Set.of();
                    }
                },
                _ -> {
                }
        );
    }

    @Test
    void setReducerIntervalRejectsNonPositiveValues() {
        var pipeline = pipelineWithNoDownloaders();

        assertThrows(IllegalArgumentException.class, () -> pipeline.setReducerIntervalInSeconds(0));
        assertThrows(IllegalArgumentException.class, () -> pipeline.setReducerIntervalInSeconds(-1));
    }

    @Test
    void setReducerIntervalReturnsPipelineAndUpdatesValue() {
        var pipeline = pipelineWithNoDownloaders();

        assertSame(pipeline, pipeline.setReducerIntervalInSeconds(3));
        assertEquals(3, pipeline.reducerIntervalInSeconds());
    }

    @Test
    void startRejectsAlreadyRunningPipeline() {
        var pipeline = pipelineWithNoDownloaders();

        try {
            pipeline.start();

            assertThrows(IllegalStateException.class, pipeline::start);
        } finally {
            pipeline.shutdownNow();
        }
    }

    @Test
    void realVivaDownloaderItemsAreNormalizedReducedAndSentToSink() throws InterruptedException {
        var logFactory = LocalItemLog.factory();
        var reduced = new CopyOnWriteArrayList<ShoppingItem>();
        var reducedItem = new CountDownLatch(1);
        var appender = new ReducedItemAppender(reduced, reducedItem);
        var logger = (Logger) LoggerFactory.getLogger("Marketi");
        var pipeline = new Pipeline(
                Set.of(new VivaItemDownloader(1, 100, 1)),
                market -> Viva.MARKET_ID.equals(market)
                        ? Optional.of(new VivaItemNormalizer())
                        : Optional.empty(),
                logFactory,
                logFactory.reducer(),
                item -> Logging.LOG.info("Pipeline sink received reduced item {}.", item)
        ).setReducerIntervalInSeconds(1);

        try {
            logger.addAppender(appender);
            appender.start();
            pipeline.start();

            assertTrue(reducedItem.await(5, TimeUnit.SECONDS));
            assertFalse(reduced.isEmpty());
            assertTrue(reduced.stream().allMatch(item -> !item.group().isBlank()));
            assertTrue(reduced.stream().allMatch(item -> !item.name().isBlank()));
            assertTrue(reduced.stream().allMatch(item -> item.markets().contains(Viva.MARKET_ID)));
            assertTrue(reduced.stream().allMatch(item -> item.marketStockCount()
                    .get(Viva.MARKET_ID)
                    .compareTo(BigDecimal.ZERO) >= 0));
            assertTrue(reduced.stream().allMatch(item -> item.marketPrices()
                    .get(Viva.MARKET_ID)
                    .price()
                    .compareTo(BigDecimal.ZERO) > 0));
            assertTrue(reduced.stream().allMatch(item -> item.quantity().multiplier().compareTo(BigDecimal.ZERO) > 0));
        } finally {
            pipeline.shutdownNow();
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private static class ReducedItemAppender extends AppenderBase<ILoggingEvent> {

        private final CopyOnWriteArrayList<ShoppingItem> reduced;
        private final CountDownLatch reducedItem;

        private ReducedItemAppender(CopyOnWriteArrayList<ShoppingItem> reduced, CountDownLatch reducedItem) {
            this.reduced = reduced;
            this.reducedItem = reducedItem;
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (!eventObject.getFormattedMessage().startsWith("Pipeline sink received reduced item ")) {
                return;
            }

            for (var argument : eventObject.getArgumentArray()) {
                if (argument instanceof ShoppingItem item) {
                    reduced.add(item);
                    reducedItem.countDown();
                }
            }
        }
    }

}
