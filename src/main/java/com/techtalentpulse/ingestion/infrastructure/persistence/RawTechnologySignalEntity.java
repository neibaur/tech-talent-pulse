package com.techtalentpulse.ingestion.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.SignalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "raw_technology_signal",
    schema = "tech_talent_pulse",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_raw_technology_signal_provider_signal",
            columnNames = {"provider", "provider_id", "signal_type"}))
public class RawTechnologySignalEntity {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 100)
  private IngestionProvider provider;

  @Column(nullable = false, length = 255)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 100)
  private SignalType signalType;

  @Column(nullable = false, length = 100)
  private String sourceTag;

  @Column(nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String payload;

  @Column(nullable = false)
  private Instant capturedAt;

  protected RawTechnologySignalEntity() {}

  public RawTechnologySignalEntity(
      IngestionProvider provider,
      String providerId,
      SignalType signalType,
      String sourceTag,
      String payload,
      Instant capturedAt) {
    this.provider = provider;
    this.providerId = providerId;
    this.signalType = signalType;
    this.sourceTag = sourceTag;
    this.payload = payload;
    this.capturedAt = capturedAt;
  }

  @PrePersist
  void assignId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }

  public UUID getId() {
    return id;
  }

  public String getProviderId() {
    return providerId;
  }

  public IngestionProvider getProvider() {
    return provider;
  }

  public SignalType getSignalType() {
    return signalType;
  }

  public String getSourceTag() {
    return sourceTag;
  }

  public String getPayload() {
    return payload;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }
}
