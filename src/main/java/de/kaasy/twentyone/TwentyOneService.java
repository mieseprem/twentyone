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

        try (ExecutorService executorService =
                Executors.newThreadPerTaskExecutor(TwentyOneApplication.THREAD_FACTORY)) {
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
