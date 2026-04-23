package com.aicbi.vectorstore;

import java.util.List;

public interface VectorBackend {
  void upsert(VectorModels.VectorRecord record);

  List<VectorModels.SearchHit> search(VectorModels.SearchRequest request);

  int count(String repoId);
}
