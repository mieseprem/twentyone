package de.kaasy.twentyone.web.config;

import de.kaasy.twentyone.TwentyOneApplication;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebConfiguration {
    @Bean
    public ClientHttpConnector getHttpClient() {
        return new ReactorClientHttpConnector(HttpClient.create()
                .headers(entries -> entries.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .responseTimeout(Duration.ofSeconds(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS)));
    }
}
