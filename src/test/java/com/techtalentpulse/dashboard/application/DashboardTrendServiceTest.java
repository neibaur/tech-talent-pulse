package com.techtalentpulse.dashboard.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techtalentpulse.dashboard.domain.TrendSummary;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.transformation.domain.TechnologyTrendMetric;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotEntity;
import com.techtalentpulse.transformation.infrastructure.persistence.TechnologyTrendSnapshotRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class DashboardTrendServiceTest {

  private final TechnologyTrendSnapshotRepository snapshotRepository =
      mock(TechnologyTrendSnapshotRepository.class);
  private final DashboardTrendService service = new DashboardTrendService(snapshotRepository);

  @Test
  void returnsRecentSnapshotsWithSafeDefaultLimit() {
    when(snapshotRepository.findAllByOrderBySnapshotDateDescTagAsc(Pageable.ofSize(50)))
        .thenReturn(List.of(snapshot("java", "2026-01-02", 10)));

    var snapshots = service.recentSnapshots(null);

    assertThat(snapshots).hasSize(1);
    assertThat(snapshots.getFirst().tag()).isEqualTo("java");
    assertThat(snapshots.getFirst().provider()).isEqualTo(IngestionProvider.STACK_OVERFLOW);
    assertThat(snapshots.getFirst().snapshotDate()).isEqualTo(LocalDate.parse("2026-01-02"));
  }

  @Test
  void capsRequestedTagHistoryLimit() {
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    when(snapshotRepository.findByTagIgnoreCaseOrderBySnapshotDateDesc(
            org.mockito.Mockito.eq("java"), pageableCaptor.capture()))
        .thenReturn(List.of());

    service.trendHistory(" java ", 1_000);

    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(500);
  }

  @Test
  void returnsSummaryWithMostRecentDateAndTopTags() {
    when(snapshotRepository.findFirstByOrderBySnapshotDateDesc())
        .thenReturn(Optional.of(snapshot("kubernetes", "2026-01-05", 15)));
    when(snapshotRepository.findTopTagsBySignalCount(Pageable.ofSize(5)))
        .thenReturn(List.of(new TestTagSignalTotal("kubernetes", 25)));

    TrendSummary summary = service.summary(null);

    assertThat(summary.mostRecentSnapshotDate()).isEqualTo(LocalDate.parse("2026-01-05"));
    assertThat(summary.topTags()).hasSize(1);
    assertThat(summary.topTags().getFirst().tag()).isEqualTo("kubernetes");
    assertThat(summary.topTags().getFirst().signalCount()).isEqualTo(25);
    verify(snapshotRepository).findTopTagsBySignalCount(Pageable.ofSize(5));
  }

  private TechnologyTrendSnapshotEntity snapshot(String tag, String snapshotDate, int signalCount) {
    return new TechnologyTrendSnapshotEntity(
        new TechnologyTrendMetric(
            LocalDate.parse(snapshotDate),
            tag,
            IngestionProvider.STACK_OVERFLOW,
            signalCount,
            3.5,
            1.5),
        Instant.parse("2026-01-06T00:00:00Z"));
  }

  private record TestTagSignalTotal(String tag, long signalCount)
      implements TechnologyTrendSnapshotRepository.TagSignalTotal {

    @Override
    public String getTag() {
      return tag;
    }

    @Override
    public long getSignalCount() {
      return signalCount;
    }
  }
}
