package com.techtalentpulse.transformation.infrastructure.persistence;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.transformation.domain.TechnologyTrendMetric;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "technology_trend_snapshot",
    schema = "tech_talent_pulse",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_technology_trend_snapshot_provider_tag_date",
            columnNames = {"provider", "tag", "snapshot_date"}))
public class TechnologyTrendSnapshotEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private LocalDate snapshotDate;

  @Column(nullable = false, length = 100)
  private String tag;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 100)
  private IngestionProvider provider;

  @Column(nullable = false)
  private int signalCount;

  @Column(nullable = false)
  private double averageScore;

  @Column(nullable = false)
  private double averageAnswerCount;

  @Column(nullable = false)
  private Instant capturedAt;

  protected TechnologyTrendSnapshotEntity() {}

  public TechnologyTrendSnapshotEntity(TechnologyTrendMetric metric, Instant capturedAt) {
    this.snapshotDate = metric.snapshotDate();
    this.tag = metric.tag();
    this.provider = metric.provider();
    this.signalCount = metric.signalCount();
    this.averageScore = metric.averageScore();
    this.averageAnswerCount = metric.averageAnswerCount();
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

  public LocalDate getSnapshotDate() {
    return snapshotDate;
  }

  public String getTag() {
    return tag;
  }

  public IngestionProvider getProvider() {
    return provider;
  }

  public int getSignalCount() {
    return signalCount;
  }

  public double getAverageScore() {
    return averageScore;
  }

  public double getAverageAnswerCount() {
    return averageAnswerCount;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }

  public void updateFrom(TechnologyTrendMetric metric, Instant capturedAt) {
    this.signalCount = metric.signalCount();
    this.averageScore = metric.averageScore();
    this.averageAnswerCount = metric.averageAnswerCount();
    this.capturedAt = capturedAt;
  }
}
