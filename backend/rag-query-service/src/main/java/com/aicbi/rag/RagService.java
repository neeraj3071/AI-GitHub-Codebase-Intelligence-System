package com.aicbi.rag;

import com.aicbi.shared.Contracts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {
    private final RestTemplate restTemplate;

    @Value("${vector.base-url:http://localhost:8084}")
    private String vectorBaseUrl;

    @Value("${llm.base-url:http://localhost:8086}")
    private String llmBaseUrl;

    public RagService(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    public Contracts.QueryResponse query(Contracts.QueryRequest request) {
        List<Double> queryEmbedding = generateDeterministicEmbedding(request.question());
        int topK = request.topK() != null ? request.topK() : 5;

        Map<String, Object> searchReq = Map.of("repoId", request.repoId(), "queryEmbedding", queryEmbedding, "topK", topK);
        List<?> hits = restTemplate.postForObject(vectorBaseUrl + "/internal/vectors/search", searchReq, List.class);

        // Map hits to Contracts.SourceSnippet - simple conversion for summary
        List<Contracts.SourceSnippet> sources = new ArrayList<>();
        // In real dev, use specific DTO for List<SearchHit> but for brevity:
        if (hits != null) {
            for (Object o : hits) {
                Map<?, ?> h = (Map<?, ?>) o;
                Map<?, ?> r = (Map<?, ?>) h.get("record");
                sources.add(new Contracts.SourceSnippet(
                        String.valueOf(r.get("filePath")),
                        String.valueOf(r.get("symbol")),
                        ((Number) r.get("startLine")).intValue(),
                        ((Number) r.get("endLine")).intValue(),
                        String.valueOf(r.get("snippet")),
                        ((Number) h.get("score")).doubleValue()));
            }
        }

        Map<String, Object> llmReq = Map.of("question", request.question(), "contexts", sources);
        Map response = restTemplate.postForObject(llmBaseUrl + "/internal/respond", llmReq, Map.class);
        String answer = response != null ? (String) response.get("answer") : "No response from LLM";

        return new Contracts.QueryResponse(answer, sources);
    }

    private List<Double> generateDeterministicEmbedding(String text) {
        int hash = text.hashCode();
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            vector.add(Math.sin(hash + i));
        }
        return vector;
    }

    private record SearchHit(Object record, double score) {}
}

@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }
}
