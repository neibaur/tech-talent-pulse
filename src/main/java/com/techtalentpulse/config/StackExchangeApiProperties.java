package com.techtalentpulse.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tech-talent-pulse.stack-exchange")
public record StackExchangeApiProperties(
    String baseUrl,
    String site,
    int defaultPageSize,
    List<String> targetTags,
    boolean schedulerEnabled) {}
