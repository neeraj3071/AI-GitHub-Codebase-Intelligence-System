package com.aicbi.apigateway;

import com.aicbi.shared.Contracts;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final WebClient webClient;

    @Value("${ingestion.base-url}")
    private String ingestionBaseUrl;

    @Value("${rag.base-url}")
    private String ragBaseUrl;

    public GatewayController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/repos/ingest")
    public Object ingest(@Valid @RequestBody Contracts.RepoIngestionRequest request) {
        return webClient.post()
                .uri(ingestionBaseUrl + "/internal/ingest")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .block(Duration.ofSeconds(30));
    }

    @PostMapping("/query")
    public Contracts.QueryResponse query(@Valid @RequestBody Contracts.QueryRequest request) {
        return webClient.post()
                .uri(ragBaseUrl + "/internal/query")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Contracts.QueryResponse.class)
                .block(Duration.ofSeconds(30));
    }
}

@Configuration
class GatewayConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
