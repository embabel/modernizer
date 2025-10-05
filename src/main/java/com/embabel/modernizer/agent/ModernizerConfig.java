package com.embabel.modernizer.agent;

import com.embabel.common.ai.model.LlmOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modernizer")
public record ModernizerConfig(
        LlmOptions analyzer,
        LlmOptions fixer
) {
}
