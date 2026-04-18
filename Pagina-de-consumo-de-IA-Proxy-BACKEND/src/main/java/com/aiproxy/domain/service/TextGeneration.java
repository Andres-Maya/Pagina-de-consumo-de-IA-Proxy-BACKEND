package com.aiproxy.domain.service;

import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;

public interface TextGeneration {
    GenerationResponse generate(GenerationRequest request);
}
