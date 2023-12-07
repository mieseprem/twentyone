package de.kaasy.twentyone.web;

import de.kaasy.twentyone.TwentyOneApplication;
import de.kaasy.twentyone.dto.CatFactResponse;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CatFactsClient {
    private static final int MAX_PARALLEL_REQUESTS = 3;

    private final WebClient webClient;
    private final Semaphore semaphore = new Semaphore(MAX_PARALLEL_REQUESTS);

    public CatFactsClient(final WebClient.Builder builder, final ClientHttpConnector httpClient) {
        webClient = builder.baseUrl("https://catfact.ninja")
                .clientConnector(httpClient)
                .build();
    }

    /**
     * This is how we can limit requests to REST service to a specific parallelism factor
     * (reason might be DOS avoidance)
     */
    public CatFactResponse fact() {
        try {
            semaphore.acquire();
            log.debug("Fetch cat fact - parallel requests: {}", MAX_PARALLEL_REQUESTS - semaphore.availablePermits());
            var catFactsResponse = webClient
                    .get()
                    .uri("/fact")
                    .retrieve()
                    .bodyToMono(CatFactResponse.class)
                    .block(Duration.ofSeconds(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS));
            log.debug("Fact received");
            return catFactsResponse;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }
}
