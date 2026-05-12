package com.techtalentpulse.transformation.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyTrendSnapshotRepository
    extends JpaRepository<TechnologyTrendSnapshotEntity, UUID> {

  Optional<TechnologyTrendSnapshotEntity> findByProviderAndTagAndSnapshotDate(
      IngestionProvider provider, String tag, LocalDate snapshotDate);
}
