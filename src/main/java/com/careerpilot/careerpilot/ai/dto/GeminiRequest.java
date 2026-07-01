package com.careerpilot.careerpilot.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeminiRequest {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Data
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @Builder
    public static class Part {
        private String text;
    }

    @Data
    @Builder
    public static class GenerationConfig {
        private int maxOutputTokens;
        private double temperature;
    }

    public static GeminiRequest of(String prompt, int maxTokens) {
        return GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(Part.builder().text(prompt).build()))
                                .build()
                ))
                .generationConfig(GenerationConfig.builder()
                        .maxOutputTokens(maxTokens)
                        .temperature(0.1)
                        .build())
                .build();
    }
}
