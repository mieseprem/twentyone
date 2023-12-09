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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TwentyOneService {
    private final CatFactsClient catFactsClient;
    private final HttpBinClient httpBinClient;
    private final YesNoClient yesNoClient;

    YesNoCatFact getData(final int batchNumber) {
        log.info("Batch: {}, Call services", batchNumber);

        Future<CatFactResponse> catFactsResponseFuture;
        Future<YesNoResponse> yesNoResponseFuture;

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            // call services in parallel - StructuredConcurrency is not yet GA in Java 21 -> do it the legacy way
            // please see
            // https://github.com/SvenWoltmann/structured-concurrency/blob/575d993378a79393cac4176ae693cc89156191a0/src/main/java/eu/happycoders/structuredconcurrency/demo1_invoice/InvoiceGenerator4b_NewVirtualThreadPerTaskCancelling.java
            // for a way to implement "Shutdown on failure"
            catFactsResponseFuture = executorService.submit(catFactsClient::fact);
            yesNoResponseFuture = executorService.submit(yesNoClient::api);

            log.debug("Batch: {}, Waiting for all services to respond", batchNumber);
        }

        if (catFactsResponseFuture.state() == Future.State.SUCCESS
                && yesNoResponseFuture.state() == Future.State.SUCCESS) {
            // no combine result of all service calls into one new value object
            try {
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
        } else if (catFactsResponseFuture.state() == Future.State.FAILED
                && !(catFactsResponseFuture.exceptionNow() instanceof InterruptedException)) {
            throw new RuntimeException(catFactsResponseFuture.exceptionNow());
        } else if (yesNoResponseFuture.state() == Future.State.FAILED
                && !(yesNoResponseFuture.exceptionNow() instanceof InterruptedException)) {
            throw new RuntimeException(yesNoResponseFuture.exceptionNow());
        } else {
            throw new RuntimeException("Futures states neither SUCCESS nor FAILED");
        }
    }

    /**
     * If you look at the code: nothing is stored here. This is just to simulate writing to a very slow disc.
     */
    void storeData(final int batchNumber, final int delay, final YesNoCatFact yesNoCatFact) {
        log.info("Batch: {}, Store result will last {}s", batchNumber, delay);
        httpBinClient.delay(delay);
        log.info(
                "Batch: {}, Result stored: {} <- {}",
                batchNumber,
                yesNoCatFact.getCatFact(),
                yesNoCatFact.getYesNoPicUrl());
    }
}
