package com.aicbi.ingestion;
import com.aicbi.shared.Contracts;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class IngestionController {
    private final IngestionService service;
    public IngestionController(IngestionService service) { this.service = service; }
    @PostMapping("/ingest")
    public Map<String, String> ingest(@Valid @RequestBody Contracts.RepoIngestionRequest request) {
        return service.ingest(request);
    }
}
