package com.careerpilot.careerpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    private List<Candidate> candidates;

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini returned no candidates");
        }
        List<Part> parts = candidates.get(0).content.parts;
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini returned empty content");
        }
        return parts.get(0).text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }
}
