package de.kaasy.twentyone.web;

import de.kaasy.twentyone.TwentyOneApplication;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class HttpBinClient {
    private final WebClient webClient;

    public HttpBinClient(final WebClient.Builder builder, final ClientHttpConnector httpClient) {
        webClient = builder.baseUrl("https://httpbin.org")
                .clientConnector(httpClient)
                .build();
    }

    public void delay(final int withDelay) {
        log.debug("Simulate delay of {}s", withDelay);
        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/delay/{delay}").build(withDelay))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS));
        log.debug("delay passed");
    }
}
