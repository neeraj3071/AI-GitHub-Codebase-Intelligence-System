package com.aicbi.llm;

import com.aicbi.shared.Contracts;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
public class LlmController {

    @PostMapping("/respond")
    public Map<String, String> respond(@RequestBody PromptRequest request) {
        String answer;
        if (request.contexts() == null || request.contexts().isEmpty()) {
            answer = "I couldn't find any relevant code context to answer: " + request.question();
        } else {
            String paths = request.contexts().stream()
                    .map(Contracts.SourceSnippet::filePath)
                    .distinct()
                    .collect(Collectors.joining(", "));
            answer = "Based on the code in [" + paths + "], I can say: [Synthetic response for '" + request.question() + "']";
        }
        return Map.of("answer", answer);
    }

    public record PromptRequest(String question, List<Contracts.SourceSnippet> contexts) {}
}
