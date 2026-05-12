package com.techtalentpulse.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techtalentpulse.analytics.domain.TagTrendComparisonResult;
import com.techtalentpulse.analytics.domain.TrendDelta;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.transformation.domain.TechnologyTrendMetric;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class TrendAnalyticsServiceTest {

  private final TechnologyTrendSnapshotRepository snapshotRepository =
      mock(TechnologyTrendSnapshotRepository.class);
  private final TrendAnalyticsService service = new TrendAnalyticsService(snapshotRepository);

  @Test
  void calculatesTrendDeltasBetweenLatestAndPreviousSnapshotDates() {
    LocalDate currentDate = LocalDate.parse("2026-01-03");
    LocalDate previousDate = LocalDate.parse("2026-01-02");
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .thenReturn(Optional.of(snapshot("java", currentDate, 20)));
    when(snapshotRepository.findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(currentDate))
        .thenReturn(Optional.of(snapshot("java", previousDate, 10)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate))
        .thenReturn(
            List.of(
                snapshot("java", currentDate, 20),
                snapshot("docker", currentDate, 15),
                snapshot("kubernetes", currentDate, 3)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(previousDate))
        .thenReturn(
            List.of(snapshot("docker", previousDate, 20), snapshot("java", previousDate, 10)));

    List<TrendDelta> deltas = service.trendDeltas(null);

    assertThat(deltas).extracting(TrendDelta::tag).containsExactly("java", "docker", "kubernetes");
    TrendDelta java = deltas.getFirst();
    assertThat(java.currentSignalCount()).isEqualTo(20);
    assertThat(java.previousSignalCount()).isEqualTo(10);
    assertThat(java.absoluteDelta()).isEqualTo(10);
    assertThat(java.percentChange()).isEqualTo(100.0);
    assertThat(java.currentRank()).isEqualTo(1);
    assertThat(java.previousRank()).isEqualTo(2);
    assertThat(java.rankMovement()).isEqualTo(1);

    TrendDelta kubernetes = deltas.get(2);
    assertThat(kubernetes.previousSignalCount()).isZero();
    assertThat(kubernetes.previousRank()).isNull();
    assertThat(kubernetes.rankMovement()).isNull();
    assertThat(kubernetes.percentChange()).isNull();
  }

  @Test
  void handlesMissingPreviousSnapshotSafely() {
    LocalDate currentDate = LocalDate.parse("2026-01-03");
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .thenReturn(Optional.of(snapshot("java", currentDate, 7)));
    when(snapshotRepository.findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(currentDate))
        .thenReturn(Optional.empty());
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate))
        .thenReturn(List.of(snapshot("java", currentDate, 7)));

    List<TrendDelta> deltas = service.trendDeltas(null);

    assertThat(deltas).hasSize(1);
    assertThat(deltas.getFirst().previousSnapshotDate()).isNull();
    assertThat(deltas.getFirst().previousSignalCount()).isZero();
    assertThat(deltas.getFirst().absoluteDelta()).isEqualTo(7);
    assertThat(deltas.getFirst().percentChange()).isNull();
    assertThat(deltas.getFirst().previousRank()).isNull();
  }

  @Test
  void avoidsDivideByZeroWhenPreviousCountIsZero() {
    LocalDate currentDate = LocalDate.parse("2026-01-03");
    LocalDate previousDate = LocalDate.parse("2026-01-02");
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .thenReturn(Optional.of(snapshot("java", currentDate, 4)));
    when(snapshotRepository.findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(currentDate))
        .thenReturn(Optional.of(snapshot("java", previousDate, 0)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate))
        .thenReturn(List.of(snapshot("java", currentDate, 4)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(previousDate))
        .thenReturn(List.of(snapshot("java", previousDate, 0)));

    List<TrendDelta> deltas = service.trendDeltas(null);

    assertThat(deltas.getFirst().previousSignalCount()).isZero();
    assertThat(deltas.getFirst().percentChange()).isNull();
  }

  @Test
  void sortsRisingTrendsByGrowthThenRankMovementThenSignalCount() {
    LocalDate currentDate = LocalDate.parse("2026-01-03");
    LocalDate previousDate = LocalDate.parse("2026-01-02");
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .thenReturn(Optional.of(snapshot("java", currentDate, 20)));
    when(snapshotRepository.findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(currentDate))
        .thenReturn(Optional.of(snapshot("java", previousDate, 10)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate))
        .thenReturn(
            List.of(
                snapshot("java", currentDate, 20),
                snapshot("postgresql", currentDate, 16),
                snapshot("docker", currentDate, 12),
                snapshot("kubernetes", currentDate, 8)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(previousDate))
        .thenReturn(
            List.of(
                snapshot("docker", previousDate, 12),
                snapshot("java", previousDate, 10),
                snapshot("postgresql", previousDate, 6),
                snapshot("kubernetes", previousDate, 6)));

    List<TrendDelta> rising = service.risingTrends(2);

    assertThat(rising).extracting(TrendDelta::tag).containsExactly("postgresql", "java");
  }

  @Test
  void appliesDefaultAndMaximumLimits() {
    assertThat(service.normalizeLimit(null)).isEqualTo(TrendAnalyticsService.DEFAULT_LIMIT);
    assertThat(service.normalizeLimit(0)).isEqualTo(TrendAnalyticsService.DEFAULT_LIMIT);
    assertThat(service.normalizeLimit(500)).isEqualTo(TrendAnalyticsService.MAX_LIMIT);
  }

  @Test
  void returnsEmptyDeltasWhenNoSnapshotsExist() {
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc()).thenReturn(Optional.empty());

    assertThat(service.trendDeltas(null)).isEmpty();

    verify(snapshotRepository).findFirstByOrderBySnapshotDateDesc();
  }

  @Test
  void comparesTagsInRequestedOrderWithLatestMetricsDeltaAndHistory() {
    LocalDate currentDate = LocalDate.parse("2026-01-03");
    LocalDate previousDate = LocalDate.parse("2026-01-02");
    when(snapshotRepository.findByNormalizedTagsOrderBySnapshotDateDescTagAsc(
            List.of("java", "python", "missing"), Pageable.ofSize(150)))
        .thenReturn(
            List.of(
                snapshot("java", currentDate, 20),
                snapshot("python", currentDate, 7),
                snapshot("java", previousDate, 10),
                snapshot("python", previousDate, 0)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(currentDate))
        .thenReturn(
            List.of(
                snapshot("java", currentDate, 20),
                snapshot("postgresql", currentDate, 12),
                snapshot("python", currentDate, 7)));
    when(snapshotRepository.findBySnapshotDateOrderByTagAsc(previousDate))
        .thenReturn(
            List.of(snapshot("java", previousDate, 10), snapshot("python", previousDate, 0)));

    TagTrendComparisonResult result = service.compareTags(List.of(" Java ", "PYTHON", "missing"));

    assertThat(result.tags())
        .extracting("normalizedTag")
        .containsExactly("java", "python", "missing");
    assertThat(result.tags().get(0).latestMetrics().signalCount()).isEqualTo(20);
    assertThat(result.tags().get(0).latestMetrics().currentRank()).isEqualTo(1);
    assertThat(result.tags().get(0).deltaMetrics().previousSignalCount()).isEqualTo(10);
    assertThat(result.tags().get(0).deltaMetrics().absoluteDelta()).isEqualTo(10);
    assertThat(result.tags().get(0).deltaMetrics().percentChange()).isEqualTo(100.0);
    assertThat(result.tags().get(0).history()).hasSize(2);
    assertThat(result.tags().get(1).deltaMetrics().percentChange()).isNull();
    assertThat(result.tags().get(2).found()).isFalse();
    assertThat(result.tags().get(2).latestMetrics()).isNull();
    assertThat(result.tags().get(2).history()).isEmpty();
  }

  @Test
  void deduplicatesCompareTagsBeforeValidationAndQuerying() {
    when(snapshotRepository.findByNormalizedTagsOrderBySnapshotDateDescTagAsc(
            eq(List.of("java", "docker")), any(Pageable.class)))
        .thenReturn(List.of());

    TagTrendComparisonResult result = service.compareTags(List.of("Java", " java ", "Docker"));

    verify(snapshotRepository)
        .findByNormalizedTagsOrderBySnapshotDateDescTagAsc(
            eq(List.of("java", "docker")), any(Pageable.class));
    assertThat(result.tags()).extracting("normalizedTag").containsExactly("java", "docker");
  }

  @Test
  void rejectsInvalidCompareTagRequests() {
    assertThatThrownBy(() -> service.compareTags(List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Compare requires at least two unique non-blank tags.");
    assertThatThrownBy(() -> service.compareTags(List.of("java")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Compare requires at least two unique non-blank tags.");
    assertThatThrownBy(
            () ->
                service.compareTags(
                    List.of("java", "docker", "kubernetes", "python", "go", "rust")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Compare supports at most five unique tags.");
  }

  private TechnologyTrendSnapshotEntity snapshot(
      String tag, LocalDate snapshotDate, int signalCount) {
    return new TechnologyTrendSnapshotEntity(
        new TechnologyTrendMetric(
            snapshotDate, tag, IngestionProvider.STACK_OVERFLOW, signalCount, 3.0, 1.0),
        Instant.parse("2026-01-04T00:00:00Z"));
  }
}
