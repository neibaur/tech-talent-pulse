package com.techtalentpulse.ingestion.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_run", schema = "tech_talent_pulse")
public class IngestionRunEntity {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 100)
  private IngestionProvider provider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private IngestionRunStatus status;

  @Column(nullable = false)
  private Instant startedAt;

  private Instant completedAt;

  @Column(columnDefinition = "text")
  private String errorMessage;

  @Column(nullable = false)
  private int itemsRequested;

  @Column(nullable = false)
  private int itemsCaptured;

  protected IngestionRunEntity() {}

  public IngestionRunEntity(
      IngestionProvider provider, IngestionRunStatus status, Instant startedAt) {
    this.provider = provider;
    this.status = status;
    this.startedAt = startedAt;
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

  public IngestionRunStatus getStatus() {
    return status;
  }

  public int getItemsRequested() {
    return itemsRequested;
  }

  public int getItemsCaptured() {
    return itemsCaptured;
  }

  public void addRequested(int count) {
    itemsRequested += count;
  }

  public void addCaptured(int count) {
    itemsCaptured += count;
  }

  public void complete(Instant completedAt) {
    this.status = IngestionRunStatus.COMPLETED;
    this.completedAt = completedAt;
  }

  public void fail(String errorMessage, Instant completedAt) {
    this.status = IngestionRunStatus.FAILED;
    this.errorMessage = errorMessage;
    this.completedAt = completedAt;
  }
}
