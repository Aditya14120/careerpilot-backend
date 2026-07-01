package com.careerpilot.careerpilot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String apiKey;
    private String apiUrl;
    private String model;
    private int maxTokens = 4096;
}
