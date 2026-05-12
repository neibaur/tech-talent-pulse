package com.techtalentpulse.dashboard.domain;

import java.time.LocalDate;
import java.util.List;

public record TrendSummary(LocalDate mostRecentSnapshotDate, List<TopTagTrend> topTags) {}
