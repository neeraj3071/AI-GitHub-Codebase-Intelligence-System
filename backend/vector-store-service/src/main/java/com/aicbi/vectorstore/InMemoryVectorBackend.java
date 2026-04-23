package com.aicbi.vectorstore;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "vector.provider", havingValue = "inmemory", matchIfMissing = true)
public class InMemoryVectorBackend implements VectorBackend {

  private final Map<String, ConcurrentMap<String, VectorModels.VectorRecord>> storage = new ConcurrentHashMap<>();

  @Override
  public void upsert(VectorModels.VectorRecord record) {
    storage.computeIfAbsent(record.repoId(), k -> new ConcurrentHashMap<>()).put(record.chunkId(), record);
  }

  @Override
  public List<VectorModels.SearchHit> search(VectorModels.SearchRequest request) {
    Collection<VectorModels.VectorRecord> records =
        storage.getOrDefault(request.repoId(), new ConcurrentHashMap<>()).values();
    return records.stream()
        .map(r -> new VectorModels.SearchHit(r, cosineSimilarity(request.queryEmbedding(), r.embedding())))
        .sorted(Comparator.comparingDouble(VectorModels.SearchHit::score).reversed())
        .limit(request.topK() != null ? request.topK() : 5)
        .collect(Collectors.toList());
  }

  @Override
  public int count(String repoId) {
    return storage.getOrDefault(repoId, new ConcurrentHashMap<>()).size();
  }

  private double cosineSimilarity(List<Double> v1, List<Double> v2) {
    if (v1 == null || v2 == null || v1.size() != v2.size()) {
      return 0.0;
    }
    double dot = 0;
    double n1 = 0;
    double n2 = 0;
    for (int i = 0; i < v1.size(); i++) {
      dot += v1.get(i) * v2.get(i);
      n1 += v1.get(i) * v1.get(i);
      n2 += v2.get(i) * v2.get(i);
    }
    if (n1 == 0 || n2 == 0) {
      return 0.0;
    }
    return dot / (Math.sqrt(n1) * Math.sqrt(n2));
  }
}
