package com.aicbi.vectorstore;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/vectors")
public class VectorStoreController {

    private final VectorBackend vectorBackend;

    public VectorStoreController(VectorBackend vectorBackend) {
        this.vectorBackend = vectorBackend;
    }

    @PostMapping("/upsert")
    public void upsert(@RequestBody VectorModels.VectorRecord record) {
        vectorBackend.upsert(record);
    }

    @PostMapping("/search")
    public List<VectorModels.SearchHit> search(@RequestBody VectorModels.SearchRequest request) {
        return vectorBackend.search(request);
    }

    @GetMapping("/{repoId}/count")
    public Map<String, Integer> count(@PathVariable String repoId) {
        return Map.of("count", vectorBackend.count(repoId));
    }
}
