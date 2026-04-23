package com.aicbi.vectorstore;

import java.util.List;

public final class VectorModels {
  private VectorModels() {}

  public record VectorRecord(
      String repoId,
      String chunkId,
      String filePath,
      String symbol,
      int startLine,
      int endLine,
      String snippet,
      List<Double> embedding) {}

  public record SearchRequest(String repoId, List<Double> queryEmbedding, Integer topK) {}

  public record SearchHit(VectorRecord record, double score) {}
}
