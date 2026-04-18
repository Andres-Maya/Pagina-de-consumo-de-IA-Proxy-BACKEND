package com.aiproxy.presentation.controller;

import com.aiproxy.application.service.TextGenerationOrchestrator;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class TextGenerationController {

    private final TextGenerationOrchestrator textGenerationOrchestrator;

    public TextGenerationController(TextGenerationOrchestrator textGenerationOrchestrator) {
        this.textGenerationOrchestrator = textGenerationOrchestrator;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(@Valid @RequestBody GenerationRequest request) {
        GenerationResponse response = textGenerationOrchestrator.generate(request);
        return ResponseEntity.ok(response);
    }
}
