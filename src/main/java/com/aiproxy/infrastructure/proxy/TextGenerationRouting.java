package com.aiproxy.infrastructure.proxy;

import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TextGenerationRouting implements TextGeneration {

    private final TextGeneration nextHandler;

    public TextGenerationRouting(@Qualifier("requestThrottlingGuard") TextGeneration nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        return nextHandler.generate(request);
    }
}
