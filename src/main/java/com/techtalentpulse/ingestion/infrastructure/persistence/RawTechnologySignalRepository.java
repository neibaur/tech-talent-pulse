package com.techtalentpulse.ingestion.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawTechnologySignalRepository
    extends JpaRepository<RawTechnologySignalEntity, UUID> {

  boolean existsByProviderAndProviderIdAndSignalType(
      IngestionProvider provider, String providerId, SignalType signalType);

  List<RawTechnologySignalEntity> findByProviderAndSignalType(
      IngestionProvider provider, SignalType signalType);
}
