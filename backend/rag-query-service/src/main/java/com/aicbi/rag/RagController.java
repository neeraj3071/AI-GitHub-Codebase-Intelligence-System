package com.aicbi.rag;

import com.aicbi.shared.Contracts;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class RagController {
    private final RagService service;
    public RagController(RagService service) { this.service = service; }

    @PostMapping("/query")
    public Contracts.QueryResponse query(@Valid @RequestBody Contracts.QueryRequest request) {
        return service.query(request);
    }
}
