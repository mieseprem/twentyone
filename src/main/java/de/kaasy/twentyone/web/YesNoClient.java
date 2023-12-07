package de.kaasy.twentyone.web;

import de.kaasy.twentyone.TwentyOneApplication;
import de.kaasy.twentyone.dto.YesNoResponse;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class YesNoClient {
    private final WebClient webClient;
    private final Lock lock = new ReentrantLock();

    public YesNoClient(final WebClient.Builder builder, final ClientHttpConnector httpClient) {
        webClient =
                builder.baseUrl("https://yesno.wtf").clientConnector(httpClient).build();
    }

    /**
     * Here a very sensitive REST service is simulated and should never be called in parallel
     */
    public YesNoResponse api() {
        lock.lock();
        try {
            log.debug("Ask for yes/no");
            var yesNoResponse = webClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder.path("/api").queryParam("force", "yes").build())
                    .retrieve()
                    .bodyToMono(YesNoResponse.class)
                    .block(Duration.ofSeconds(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS));
            if (yesNoResponse == null) {
                throw new RuntimeException("No response from YesNo service received");
            }
            log.debug("Answer is: {}", yesNoResponse.getAnswer());
            return yesNoResponse;
        } finally {
            lock.unlock();
        }
    }
}
