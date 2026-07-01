package com.careerpilot.careerpilot.ai;

import com.careerpilot.careerpilot.ai.dto.GeminiRequest;
import com.careerpilot.careerpilot.ai.dto.GeminiResponse;
import com.careerpilot.careerpilot.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final AiProperties aiProperties;

    public GeminiClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.getApiUrl())
                .build();
    }

    public String generate(String prompt) {
        String uri = "/models/" + aiProperties.getModel()
                + ":generateContent?key=" + aiProperties.getApiKey();

        GeminiRequest request = GeminiRequest.of(prompt, aiProperties.getMaxTokens());

        log.debug("Sending request to Gemini model: {}", aiProperties.getModel());

        GeminiResponse response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null) {
            throw new RuntimeException("Gemini API returned null response");
        }

        return response.extractText();
    }
}
