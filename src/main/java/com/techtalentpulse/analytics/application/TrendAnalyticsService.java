package com.techtalentpulse.analytics.application;

import com.techtalentpulse.analytics.domain.TagTrendComparison;
import com.techtalentpulse.analytics.domain.TagTrendComparisonResult;
import com.techtalentpulse.analytics.domain.TagTrendDeltaMetrics;
import com.techtalentpulse.analytics.domain.TagTrendHistoryPoint;
import com.techtalentpulse.analytics.domain.TagTrendLatestMetrics;
import com.techtalentpulse.analytics.domain.TrendDelta;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrendAnalyticsService {

  public static final int DEFAULT_LIMIT = 25;
  public static final int MAX_LIMIT = 100;
  public static final int MIN_COMPARE_TAGS = 2;
  public static final int MAX_COMPARE_TAGS = 5;
  public static final int DEFAULT_COMPARE_HISTORY_LIMIT = 30;

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

  @Transactional(readOnly = true)
  public TagTrendComparisonResult compareTags(List<String> requestedTags) {
    List<RequestedTag> normalizedTags = normalizeCompareTags(requestedTags);
    List<TechnologyTrendSnapshotEntity> snapshots =
        snapshotRepository.findByNormalizedTagsOrderBySnapshotDateDescTagAsc(
            normalizedTags.stream().map(RequestedTag::normalizedTag).toList(),
            PageRequest.of(0, MAX_COMPARE_TAGS * DEFAULT_COMPARE_HISTORY_LIMIT));

    Map<String, List<TechnologyTrendSnapshotEntity>> snapshotsByTag =
        snapshots.stream()
            .collect(
                Collectors.groupingBy(
                    snapshot -> snapshot.getTag().toLowerCase(),
                    LinkedHashMap::new,
                    Collectors.toList()));
    Map<LocalDate, Map<String, Integer>> ranksByDate = new LinkedHashMap<>();

    List<TagTrendComparison> comparisons =
        normalizedTags.stream()
            .map(
                tag ->
                    toTagTrendComparison(
                        tag,
                        snapshotsByTag.getOrDefault(tag.normalizedTag(), List.of()),
                        ranksByDate))
            .toList();
    return new TagTrendComparisonResult(comparisons);
  }

  public List<String> validateAndNormalizeCompareTags(List<String> requestedTags) {
    return normalizeCompareTags(requestedTags).stream().map(RequestedTag::normalizedTag).toList();
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

  private TagTrendComparison toTagTrendComparison(
      RequestedTag requestedTag,
      List<TechnologyTrendSnapshotEntity> snapshots,
      Map<LocalDate, Map<String, Integer>> ranksByDate) {
    if (snapshots.isEmpty()) {
      return new TagTrendComparison(
          requestedTag.rawTag(), requestedTag.normalizedTag(), false, null, null, List.of());
    }

    TechnologyTrendSnapshotEntity latest = snapshots.getFirst();
    TechnologyTrendSnapshotEntity previous = snapshots.size() > 1 ? snapshots.get(1) : null;
    Integer currentRank = rankForTagOnDate(latest.getTag(), latest.getSnapshotDate(), ranksByDate);
    Integer previousRank =
        previous == null
            ? null
            : rankForTagOnDate(previous.getTag(), previous.getSnapshotDate(), ranksByDate);
    TagTrendLatestMetrics latestMetrics =
        new TagTrendLatestMetrics(
            latest.getSnapshotDate(),
            latest.getSignalCount(),
            latest.getAverageScore(),
            latest.getAverageAnswerCount(),
            currentRank);
    TagTrendDeltaMetrics deltaMetrics =
        new TagTrendDeltaMetrics(
            previous == null ? null : previous.getSnapshotDate(),
            previous == null ? 0 : previous.getSignalCount(),
            latest.getSignalCount() - (previous == null ? 0 : previous.getSignalCount()),
            percentChange(
                latest.getSignalCount(),
                previous == null ? 0 : previous.getSignalCount(),
                previous),
            previousRank,
            currentRank == null || previousRank == null ? null : previousRank - currentRank);
    List<TagTrendHistoryPoint> history =
        snapshots.stream()
            .limit(DEFAULT_COMPARE_HISTORY_LIMIT)
            .map(
                snapshot ->
                    new TagTrendHistoryPoint(
                        snapshot.getSnapshotDate(),
                        snapshot.getSignalCount(),
                        snapshot.getAverageScore(),
                        snapshot.getAverageAnswerCount()))
            .toList();

    return new TagTrendComparison(
        requestedTag.rawTag(),
        requestedTag.normalizedTag(),
        true,
        latestMetrics,
        deltaMetrics,
        history);
  }

  private Integer rankForTagOnDate(
      String tag, LocalDate snapshotDate, Map<LocalDate, Map<String, Integer>> ranksByDate) {
    return ranksByDate
        .computeIfAbsent(
            snapshotDate,
            date -> ranksByTag(snapshotRepository.findBySnapshotDateOrderByTagAsc(date)))
        .get(tag);
  }

  private List<RequestedTag> normalizeCompareTags(List<String> requestedTags) {
    if (requestedTags == null) {
      throw new IllegalArgumentException("At least two tags are required.");
    }

    Map<String, String> normalizedTags = new LinkedHashMap<>();
    for (String requestedTag : requestedTags) {
      if (requestedTag == null || requestedTag.trim().isEmpty()) {
        continue;
      }
      String normalizedTag = requestedTag.trim().toLowerCase();
      normalizedTags.putIfAbsent(normalizedTag, requestedTag.trim());
    }

    if (normalizedTags.size() < MIN_COMPARE_TAGS) {
      throw new IllegalArgumentException("At least two unique tags are required.");
    }
    if (normalizedTags.size() > MAX_COMPARE_TAGS) {
      throw new IllegalArgumentException("At most five unique tags can be compared.");
    }

    List<RequestedTag> tags = new ArrayList<>();
    normalizedTags.forEach(
        (normalizedTag, rawTag) -> tags.add(new RequestedTag(rawTag, normalizedTag)));
    return tags;
  }

  private record RequestedTag(String rawTag, String normalizedTag) {}
}
