package com.careerpilot.careerpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeminiRequest {

    private String model;
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private double temperature;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }

    public static GeminiRequest of(String prompt, int maxTokens) {
        return GeminiRequest.builder()
                .messages(List.of(
                        Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .maxTokens(maxTokens)
                .temperature(0.1)
                .build();
    }
}
