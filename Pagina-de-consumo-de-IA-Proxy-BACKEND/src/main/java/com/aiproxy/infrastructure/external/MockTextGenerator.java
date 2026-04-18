package com.aiproxy.infrastructure.external;

import com.aiproxy.domain.service.TextGeneration;
import com.aiproxy.presentation.dto.GenerationRequest;
import com.aiproxy.presentation.dto.GenerationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class MockTextGenerator implements TextGeneration {

    private static final List<String> MOCK_RESPONSES = List.of(
            "The quantum computing revolution promises to transform cryptography and drug discovery through unprecedented processing power.",
            "Sustainable agriculture practices combined with vertical farming could solve global food security challenges by 2050.",
            "Neural networks mimicking human brain structures have achieved remarkable breakthroughs in natural language understanding.",
            "Climate change mitigation requires immediate action across renewable energy, carbon capture, and circular economy initiatives.",
            "Blockchain technology extends beyond cryptocurrency into supply chain transparency and decentralized identity verification."
    );

    private final Random random = new Random();

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Text generation was interrupted", e);
        }

        String selectedResponse = MOCK_RESPONSES.get(random.nextInt(MOCK_RESPONSES.size()));
        int tokensUsed = request.getPrompt().length() / 4 + selectedResponse.length() / 4;

        return new GenerationResponse(
                selectedResponse,
                tokensUsed,
                System.currentTimeMillis()
        );
    }
}
