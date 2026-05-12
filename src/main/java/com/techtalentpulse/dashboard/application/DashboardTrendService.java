package com.techtalentpulse.dashboard.application;

import com.techtalentpulse.dashboard.domain.TopTagTrend;
import com.techtalentpulse.dashboard.domain.TrendSnapshot;
import com.techtalentpulse.dashboard.domain.TrendSummary;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardTrendService {

  public static final int DEFAULT_LIMIT = 50;
  public static final int DEFAULT_SUMMARY_LIMIT = 5;
  public static final int MAX_LIMIT = 500;

  private final TechnologyTrendSnapshotRepository snapshotRepository;

  public DashboardTrendService(TechnologyTrendSnapshotRepository snapshotRepository) {
    this.snapshotRepository = snapshotRepository;
  }

  @Transactional(readOnly = true)
  public List<TrendSnapshot> recentSnapshots(Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit, DEFAULT_LIMIT);
    return snapshotRepository
        .findAllByOrderBySnapshotDateDescTagAsc(PageRequest.of(0, limit))
        .stream()
        .map(this::toTrendSnapshot)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TrendSnapshot> trendHistory(String tag, Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit, DEFAULT_LIMIT);
    return snapshotRepository
        .findByTagIgnoreCaseOrderBySnapshotDateDesc(tag.trim(), PageRequest.of(0, limit))
        .stream()
        .map(this::toTrendSnapshot)
        .toList();
  }

  @Transactional(readOnly = true)
  public TrendSummary summary(Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit, DEFAULT_SUMMARY_LIMIT);
    List<TopTagTrend> topTags =
        snapshotRepository.findTopTagsBySignalCount(PageRequest.of(0, limit)).stream()
            .map(total -> new TopTagTrend(total.getTag(), total.getSignalCount()))
            .toList();

    return new TrendSummary(
        snapshotRepository
            .findFirstByOrderBySnapshotDateDesc()
            .map(TechnologyTrendSnapshotEntity::getSnapshotDate)
            .orElse(null),
        topTags);
  }

  private int normalizeLimit(Integer requestedLimit, int defaultLimit) {
    if (requestedLimit == null || requestedLimit < 1) {
      return defaultLimit;
    }
    return Math.min(requestedLimit, MAX_LIMIT);
  }

  private TrendSnapshot toTrendSnapshot(TechnologyTrendSnapshotEntity snapshot) {
    return new TrendSnapshot(
        snapshot.getTag(),
        snapshot.getProvider(),
        snapshot.getSnapshotDate(),
        snapshot.getSignalCount(),
        snapshot.getAverageScore(),
        snapshot.getAverageAnswerCount());
  }
}
