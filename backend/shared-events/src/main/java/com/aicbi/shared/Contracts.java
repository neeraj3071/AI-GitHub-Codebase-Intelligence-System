package com.aicbi.shared;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class Contracts {
  private Contracts() {}

    public record RepoIngestionRequest(
            @NotBlank @Pattern(regexp = "https?://.+", message = "repoUrl must be a valid HTTP/HTTPS URL") String repoUrl,
            String branch,
            boolean privateRepo,
            String oauthToken) {}

  public record RepoClonedEvent(String repoId, String repoUrl, String localPath, Instant clonedAt) {}

  public record FileChunkedEvent(
      String repoId,
      String chunkId,
      String filePath,
      String symbol,
      int startLine,
      int endLine,
      String code) {}

  public record EmbeddingReadyEvent(
      String repoId,
      String chunkId,
      String vectorId,
      String filePath,
      String symbol,
      Instant embeddedAt) {}

  public record SourceSnippet(
      String filePath,
      String symbol,
      int startLine,
      int endLine,
      String snippet,
      double score) {}

    public record QueryRequest(
            @NotBlank String repoId,
            @NotBlank @Size(max = 2000) String question,
            @Min(1) @Max(20) Integer topK,
            String sessionId) {}

  public record QueryResponse(String answer, List<SourceSnippet> sources) {}
}
