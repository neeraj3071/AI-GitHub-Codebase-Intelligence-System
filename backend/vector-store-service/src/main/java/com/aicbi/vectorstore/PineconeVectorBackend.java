package com.aicbi.vectorstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "vector.provider", havingValue = "pinecone")
public class PineconeVectorBackend implements VectorBackend {

  private final WebClient webClient;

  public PineconeVectorBackend(
      WebClient.Builder builder,
      @Value("${pinecone.index-url:http://localhost:5080}") String indexUrl,
      @Value("${pinecone.api-key:}") String apiKey) {
    this.webClient =
        builder
            .baseUrl(indexUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Api-Key", apiKey)
            .build();
  }

  @Override
  public void upsert(VectorModels.VectorRecord record) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("repoId", record.repoId());
    metadata.put("chunkId", record.chunkId());
    metadata.put("filePath", record.filePath());
    metadata.put("symbol", record.symbol());
    metadata.put("startLine", record.startLine());
    metadata.put("endLine", record.endLine());
    metadata.put("snippet", record.snippet());

    Map<String, Object> vector = new HashMap<>();
    vector.put("id", record.chunkId());
    vector.put("values", record.embedding());
    vector.put("metadata", metadata);

    Map<String, Object> request = new HashMap<>();
    request.put("namespace", record.repoId());
    request.put("vectors", List.of(vector));

    webClient.post().uri("/vectors/upsert").bodyValue(request).retrieve().bodyToMono(Map.class).block();
  }

  @Override
  public List<VectorModels.SearchHit> search(VectorModels.SearchRequest request) {
    int topK = request.topK() != null ? request.topK() : 5;

    Map<String, Object> queryReq = new HashMap<>();
    queryReq.put("namespace", request.repoId());
    queryReq.put("vector", request.queryEmbedding());
    queryReq.put("topK", topK);
    queryReq.put("includeMetadata", true);

    Map<?, ?> response =
        webClient.post().uri("/query").bodyValue(queryReq).retrieve().bodyToMono(Map.class).block();

    if (response == null || !(response.get("matches") instanceof List<?> matches)) {
      return List.of();
    }

    List<VectorModels.SearchHit> hits = new ArrayList<>();
    for (Object matchObj : matches) {
      if (!(matchObj instanceof Map<?, ?> match)) {
        continue;
      }
      Object metadataObj = match.get("metadata");
      if (!(metadataObj instanceof Map<?, ?> metadata)) {
        continue;
      }

      String chunkId = String.valueOf(match.get("id"));
      String filePath = String.valueOf(metadata.get("filePath"));
      String symbol = String.valueOf(metadata.get("symbol"));
      int startLine = numberToInt(metadata.get("startLine"));
      int endLine = numberToInt(metadata.get("endLine"));
      String snippet = String.valueOf(metadata.get("snippet"));
      double score = numberToDouble(match.get("score"));

      VectorModels.VectorRecord record =
          new VectorModels.VectorRecord(
              request.repoId(), chunkId, filePath, symbol, startLine, endLine, snippet, List.of());
      hits.add(new VectorModels.SearchHit(record, score));
    }

    return hits;
  }

  @Override
  public int count(String repoId) {
    Map<String, Object> req = Map.of("namespace", repoId);
    Map<?, ?> response =
        webClient
            .post()
            .uri("/describe_index_stats")
            .bodyValue(req)
            .retrieve()
            .bodyToMono(Map.class)
            .block();
    if (response == null) {
      return 0;
    }
    Object total = response.get("totalVectorCount");
    return numberToInt(total);
  }

  private int numberToInt(Object value) {
    return value instanceof Number n ? n.intValue() : 0;
  }

  private double numberToDouble(Object value) {
    return value instanceof Number n ? n.doubleValue() : 0.0;
  }
}
