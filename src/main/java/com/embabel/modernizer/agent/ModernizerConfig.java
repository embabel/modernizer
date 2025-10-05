package com.embabel.modernizer.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modernizer")
public record ModernizerConfig(
        boolean branchMode
) {
}
