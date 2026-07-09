package com.careerpilot.careerpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    private List<Choice> choices;

    public String extractText() {
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Groq returned no choices. Check your API key and model.");
        }
        Choice choice = choices.get(0);
        if (choice.message == null || choice.message.content == null || choice.message.content.isBlank()) {
            throw new RuntimeException("Groq returned an empty response.");
        }
        return choice.message.content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }
}
