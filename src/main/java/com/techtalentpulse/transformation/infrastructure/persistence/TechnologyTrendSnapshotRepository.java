package com.techtalentpulse.transformation.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TechnologyTrendSnapshotRepository
    extends JpaRepository<TechnologyTrendSnapshotEntity, UUID> {

  Optional<TechnologyTrendSnapshotEntity> findByProviderAndTagAndSnapshotDate(
      IngestionProvider provider, String tag, LocalDate snapshotDate);

  List<TechnologyTrendSnapshotEntity> findAllByOrderBySnapshotDateDescTagAsc(Pageable pageable);

  List<TechnologyTrendSnapshotEntity> findBySnapshotDateOrderByTagAsc(LocalDate snapshotDate);

  List<TechnologyTrendSnapshotEntity> findByTagIgnoreCaseOrderBySnapshotDateDesc(
      String tag, Pageable pageable);

  Optional<TechnologyTrendSnapshotEntity> findFirstByOrderBySnapshotDateDesc();

  Optional<TechnologyTrendSnapshotEntity> findFirstBySnapshotDateBeforeOrderBySnapshotDateDesc(
      LocalDate snapshotDate);

  @Query(
      """
      select snapshot.tag as tag, sum(snapshot.signalCount) as signalCount
      from TechnologyTrendSnapshotEntity snapshot
      group by snapshot.tag
      order by sum(snapshot.signalCount) desc, snapshot.tag asc
      """)
  List<TagSignalTotal> findTopTagsBySignalCount(Pageable pageable);

  interface TagSignalTotal {

    String getTag();

    long getSignalCount();
  }
}
