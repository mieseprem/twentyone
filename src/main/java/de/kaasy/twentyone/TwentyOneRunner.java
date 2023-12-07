package de.kaasy.twentyone;

import de.kaasy.twentyone.dto.CatFactResponse;
import de.kaasy.twentyone.dto.YesNoResponse;
import de.kaasy.twentyone.value.YesNoCatFact;
import de.kaasy.twentyone.web.CatFactsClient;
import de.kaasy.twentyone.web.HttpBinClient;
import de.kaasy.twentyone.web.YesNoClient;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwentyOneRunner {
    private final CatFactsClient catFactsClient;
    private final HttpBinClient httpBinClient;
    private final YesNoClient yesNoClient;

    private static final ThreadFactory THREAD_FACTORY =
            Thread.ofVirtual().name("virt-", 0L).factory();

    void doYourWork() {
        log.info("Started");
        // if you really want to limit the parallelism
        try (var executorService = Executors.newFixedThreadPool(10, THREAD_FACTORY)) {
            AtomicInteger batchProcess = new AtomicInteger();

            // Simulate workload by generating a series of integers we'll later use as delay value
            ThreadLocalRandom.current()
                    .ints(20, 1, Math.toIntExact(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS) - 1)
                    .forEach(intUsedForEverything -> {
                        var batchNumber = batchProcess.incrementAndGet();

                        executorService.submit(() -> {
                            // vvvvvvvv "business logic" vvvvvvvv
                            // #
                            // # for every workload item data has to be collected
                            var yesNoCatFact = getData(batchNumber);
                            // #
                            // # afterward data should be written to somewhere
                            storeData(batchNumber, intUsedForEverything, yesNoCatFact);
                            // #
                            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                        });
                    });
        }
        log.info("Done");
    }

    private YesNoCatFact getData(final int batchNumber) {
        log.info("Batch: {}, Call services", batchNumber);

        try (ExecutorService executorService = Executors.newThreadPerTaskExecutor(THREAD_FACTORY)) {
            // call services in parallel - StructuredConcurrency is not yet GA in Java 21 -> do it the legacy way
            Future<CatFactResponse> catFactsResponseFuture = executorService.submit(catFactsClient::fact);
            Future<YesNoResponse> yesNoResponseFuture = executorService.submit(yesNoClient::api);

            // here we would loop and check if one service has failed to cancel the other one

            // no combine result of all service calls into one new value object
            var yesNoCatFact = YesNoCatFact.builder()
                    .withYesNoValue(yesNoResponseFuture.get().getAnswer())
                    .withYesNoPicUrl(yesNoResponseFuture.get().getImage())
                    .withCatFact(catFactsResponseFuture.get().getFact())
                    .build();
            log.debug("Batch: {}, All services have responded", batchNumber);
            return yesNoCatFact;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If you look at the code: nothing is stored here. This is just to simulate writing to a very slow disc.
     */
    private void storeData(final int batchNumber, final int delay, final YesNoCatFact yesNoCatFact) {
        log.info("Batch: {}, Store result will last {}s", batchNumber, delay);
        httpBinClient.delay(delay);
        log.info(
                "Batch: {}, Result stored: {} <- {}",
                batchNumber,
                yesNoCatFact.getCatFact(),
                yesNoCatFact.getYesNoPicUrl());
    }
}
