package com.techtalentpulse.ingestion.domain;

public enum IngestionRunStatus {
  STARTED,
  RUNNING,
  COMPLETED,
  COMPLETED_ZERO_RECORDS,
  FAILED
}
