package com.aicbi.embedding;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.aicbi.shared.Contracts;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EmbeddingListener {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingListener.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${vector.base-url:http://localhost:8084}")
    private String vectorBaseUrl;

    public EmbeddingListener(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "file-chunked", groupId = "embedding-service")
    public void onFileChunked(String payload) {
        try {
            Contracts.FileChunkedEvent event = objectMapper.readValue(payload, Contracts.FileChunkedEvent.class);
            List<Double> embedding = generateDeterministicEmbedding(event.code());
            
            UpsertRequest upsert = new UpsertRequest(
                    event.repoId(), event.chunkId(), event.filePath(), event.symbol(),
                    event.startLine(), event.endLine(), event.code(), embedding);
            
            restTemplate.postForObject(vectorBaseUrl + "/internal/vectors/upsert", upsert, Object.class);

            Contracts.EmbeddingReadyEvent ready = new Contracts.EmbeddingReadyEvent(
                    event.repoId(), event.chunkId(), UUID.randomUUID().toString(),
                    event.filePath(), event.symbol(), Instant.now());
            kafkaTemplate.send("embedding-ready", objectMapper.writeValueAsString(ready));
            log.debug("Embedding generated repoId={} chunkId={}", event.repoId(), event.chunkId());
        } catch (Exception e) {
            log.error("Embedding flow failed: {}", e.getMessage(), e);
        }
    }

    private List<Double> generateDeterministicEmbedding(String text) {
        int hash = text.hashCode();
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            vector.add(Math.sin(hash + i));
        }
        return vector;
    }

    public record UpsertRequest(
            String repoId, String chunkId, String filePath, String symbol,
            int startLine, int endLine, String snippet, List<Double> embedding) {}
}

@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }
}
