package com.careerpilot.careerpilot.ai;

import com.careerpilot.careerpilot.ai.dto.GeminiRequest;
import com.careerpilot.careerpilot.ai.dto.GeminiResponse;
import com.careerpilot.careerpilot.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
        GeminiRequest request = GeminiRequest.of(prompt, aiProperties.getMaxTokens());
        request.setModel(aiProperties.getModel());

        log.debug("Sending request to Groq model: {}", aiProperties.getModel());

        try {
            GeminiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null) {
                throw new RuntimeException("Groq API returned null response");
            }

            return response.extractText();

        } catch (HttpClientErrorException e) {
            log.error("Groq API client error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("Groq API key is invalid or expired. Update your key in application-local.properties.");
            }
            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Groq API rate limit exceeded. Please wait and try again.");
            }
            throw new RuntimeException("Groq API error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Groq API server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq API is temporarily unavailable. Please try again later.");
        } catch (RestClientException e) {
            log.error("Groq API connection error", e);
            throw new RuntimeException("Could not connect to Groq API. Check your internet connection.");
        }
    }
}
