package com.techtalentpulse.analytics.domain;

import java.util.List;

public record TagTrendComparison(
    String requestedTag,
    String normalizedTag,
    boolean found,
    TagTrendLatestMetrics latestMetrics,
    TagTrendDeltaMetrics deltaMetrics,
    List<TagTrendHistoryPoint> history) {}
