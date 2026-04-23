package com.aicbi.ingestion;

import com.aicbi.shared.Contracts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final RepoRecordRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.repo-storage-root:/tmp/aicbi/repos}")
    private String storageRoot;

    public IngestionService(RepoRecordRepository repository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, String> ingest(Contracts.RepoIngestionRequest request) {
        String repoId = UUID.randomUUID().toString();
        String clonePath = storageRoot + "/" + repoId;
        try {
            Files.createDirectories(Path.of(storageRoot));
            var cloneCommand = Git.cloneRepository()
                    .setURI(request.repoUrl())
                    .setDirectory(new File(clonePath));
            if (request.branch() != null && !request.branch().isBlank()) {
                cloneCommand.setBranch(request.branch());
            }
            if (request.privateRepo() && request.oauthToken() != null && !request.oauthToken().isBlank()) {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(request.oauthToken(), ""));
            }
            cloneCommand.call().close();

            RepoRecord record = new RepoRecord(repoId, request.repoUrl(), clonePath, Instant.now());
            repository.save(record);

            Contracts.RepoClonedEvent event = new Contracts.RepoClonedEvent(repoId, request.repoUrl(), clonePath, record.getCreatedAt());
            kafkaTemplate.send("repo-cloned", objectMapper.writeValueAsString(event));

            log.info("Repository cloned and event published. repoId={}, path={}", repoId, clonePath);
            return Map.of("repoId", repoId, "status", "CLONED");
        } catch (Exception e) {
            log.error("Ingestion failed for repoUrl={}: {}", request.repoUrl(), e.getMessage(), e);
            throw new IllegalStateException("Ingestion failed", e);
        }
    }
}
