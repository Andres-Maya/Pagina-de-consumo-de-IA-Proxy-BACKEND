package com.aiproxy.application.service;

import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import org.springframework.stereotype.Service;

@Service
public class TextGenerationOrchestrator implements TextGeneration {

    private final TextGeneration textGeneration;

    public TextGenerationOrchestrator(TextGeneration textGeneration) {
        this.textGeneration = textGeneration;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        return textGeneration.generate(request);
    }
}
