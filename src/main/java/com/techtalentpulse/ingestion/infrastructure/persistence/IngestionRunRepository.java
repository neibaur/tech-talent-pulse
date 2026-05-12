package com.techtalentpulse.ingestion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRunRepository extends JpaRepository<IngestionRunEntity, UUID> {

  List<IngestionRunEntity> findAllByOrderByStartedAtDescCompletedAtDesc(Pageable pageable);
}
