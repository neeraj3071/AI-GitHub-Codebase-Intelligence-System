package com.aicbi.chunking;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.aicbi.shared.Contracts;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChunkingListener {
    private static final Logger log = LoggerFactory.getLogger(ChunkingListener.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ChunkingListener(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "repo-cloned", groupId = "code-chunking-service")
    public void onRepoCloned(String payload) {
        try {
            Contracts.RepoClonedEvent event = objectMapper.readValue(payload, Contracts.RepoClonedEvent.class);
            Path root = Path.of(event.localPath());
            log.info("Chunking started for repoId={} path={}", event.repoId(), root);
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                    .filter(path -> isSupported(path.toString()))
                    .filter(path -> !isIgnored(path))
                    .forEach(path -> chunkFile(event.repoId(), path, root));
            }
            log.info("Chunking completed for repoId={}", event.repoId());
        } catch (Exception e) {
            log.error("Chunking failed: {}", e.getMessage(), e);
        }
    }

    private boolean isSupported(String name) {
        return name.endsWith(".java") || name.endsWith(".py") || name.endsWith(".js") || name.endsWith(".ts") ||
               name.endsWith(".tsx") || name.endsWith(".cpp") || name.endsWith(".go") || name.endsWith(".md") ||
               name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".json");
    }

    private boolean isIgnored(Path path) {
        String p = path.toString().replace('\\', '/');
        return p.contains("/.git/") || p.contains("/node_modules/") || p.contains("/target/")
            || p.contains("/dist/") || p.contains("/build/");
    }

    private void chunkFile(String repoId, Path path, Path root) {
        try {
            List<String> lines = Files.readAllLines(path);
            String relativePath = root.relativize(path).toString();
            for (int i = 0; i < lines.size(); i += 120) {
                int end = Math.min(i + 120, lines.size());
                String code = String.join("\n", lines.subList(i, end));
                Contracts.FileChunkedEvent chunkedEvent = new Contracts.FileChunkedEvent(
                        repoId, UUID.randomUUID().toString(), relativePath, "", i + 1, end, code);
                kafkaTemplate.send("file-chunked", objectMapper.writeValueAsString(chunkedEvent));
            }
        } catch (Exception e) {
            log.error("Failed to chunk file {}: {}", path, e.getMessage(), e);
        }
    }
}
