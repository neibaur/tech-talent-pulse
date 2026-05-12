package com.techtalentpulse.analytics.application;

import com.techtalentpulse.analytics.domain.TrendDelta;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrendAnalyticsService {

  public static final int DEFAULT_LIMIT = 25;
  public static final int MAX_LIMIT = 100;

  private final TechnologyTrendSnapshotRepository snapshotRepository;

  public TrendAnalyticsService(TechnologyTrendSnapshotRepository snapshotRepository) {
    this.snapshotRepository = snapshotRepository;
  }

  @Transactional(readOnly = true)
  public List<TrendDelta> trendDeltas(Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit);
    return calculateTrendDeltas().stream()
        .sorted(Comparator.comparingInt(TrendDelta::currentRank))
        .limit(limit)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TrendDelta> risingTrends(Integer requestedLimit) {
    int limit = normalizeLimit(requestedLimit);
    return calculateTrendDeltas().stream()
        .filter(delta -> delta.absoluteDelta() > 0 || positiveRankMovement(delta))
        .sorted(
            Comparator.comparingInt(TrendDelta::absoluteDelta)
                .reversed()
                .thenComparing(
                    TrendAnalyticsService::rankMovementForSort, Comparator.reverseOrder())
                .thenComparing(TrendDelta::currentSignalCount, Comparator.reverseOrder())
                .thenComparing(TrendDelta::tag))
        .limit(limit)
        .toList();
  }

  public int normalizeLimit(Integer requestedLimit) {
    if (requestedLimit == null || requestedLimit < 1) {
      return DEFAULT_LIMIT;
    }
    return Math.min(requestedLimit, MAX_LIMIT);
  }

  private List<TrendDelta> calculateTrendDeltas() {
    Optional<TechnologyTrendSnapshotEntity> latestSnapshot =
        snapshotRepository.findFirstByOrderBySnapshotDateDesc();
    if (latestSnapshot.isEmpty()) {
      return List.of();
    }

    LocalDate currentDate = latestSnapshot.get().getSnapshotDate();
    LocalDate previousDate =
        snapshotRepository
            .findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(currentDate)
            .map(TechnologyTrendSnapshotEntity::getSnapshotDate)
            .orElse(null);

    List<TechnologyTrendSnapshotEntity> currentSnapshots =
        snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate);
    List<TechnologyTrendSnapshotEntity> previousSnapshots =
        previousDate == null
            ? List.of()
            : snapshotRepository.findBySnapshotDateOrderByTagAsc(previousDate);

    Map<String, TechnologyTrendSnapshotEntity> previousByTag =
        previousSnapshots.stream()
            .collect(Collectors.toMap(TechnologyTrendSnapshotEntity::getTag, Function.identity()));
    Map<String, Integer> previousRanks = ranksByTag(previousSnapshots);
    Map<String, Integer> currentRanks = ranksByTag(currentSnapshots);

    return currentSnapshots.stream()
        .map(
            current -> {
              TechnologyTrendSnapshotEntity previous = previousByTag.get(current.getTag());
              int currentSignalCount = current.getSignalCount();
              int previousSignalCount = previous == null ? 0 : previous.getSignalCount();
              int absoluteDelta = currentSignalCount - previousSignalCount;
              Integer previousRank = previousRanks.get(current.getTag());
              int currentRank = currentRanks.get(current.getTag());
              return new TrendDelta(
                  current.getTag(),
                  currentDate,
                  previousDate,
                  currentSignalCount,
                  previousSignalCount,
                  absoluteDelta,
                  percentChange(currentSignalCount, previousSignalCount, previous),
                  currentRank,
                  previousRank,
                  previousRank == null ? null : previousRank - currentRank);
            })
        .toList();
  }

  private Map<String, Integer> ranksByTag(List<TechnologyTrendSnapshotEntity> snapshots) {
    List<TechnologyTrendSnapshotEntity> rankedSnapshots =
        snapshots.stream()
            .sorted(
                Comparator.comparingInt(TechnologyTrendSnapshotEntity::getSignalCount)
                    .reversed()
                    .thenComparing(TechnologyTrendSnapshotEntity::getTag))
            .toList();

    return java.util.stream.IntStream.range(0, rankedSnapshots.size())
        .boxed()
        .collect(
            Collectors.toMap(index -> rankedSnapshots.get(index).getTag(), index -> index + 1));
  }

  private static Double percentChange(
      int currentSignalCount, int previousSignalCount, TechnologyTrendSnapshotEntity previous) {
    if (previous == null || previousSignalCount == 0) {
      return null;
    }
    return ((double) currentSignalCount - previousSignalCount) / previousSignalCount * 100.0;
  }

  private static boolean positiveRankMovement(TrendDelta delta) {
    return delta.rankMovement() != null && delta.rankMovement() > 0;
  }

  private static int rankMovementForSort(TrendDelta delta) {
    return delta.rankMovement() == null ? Integer.MIN_VALUE : delta.rankMovement();
  }
}
