CREATE TABLE IF NOT EXISTS tech_talent_pulse.ingestion_run (
    id UUID PRIMARY KEY,
    provider VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    error_message TEXT,
    items_requested INTEGER NOT NULL DEFAULT 0,
    items_captured INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ingestion_run_provider_started_at
    ON tech_talent_pulse.ingestion_run (provider, started_at DESC);

CREATE TABLE IF NOT EXISTS tech_talent_pulse.raw_technology_signal (
    id UUID PRIMARY KEY,
    provider VARCHAR(100) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    signal_type VARCHAR(100) NOT NULL,
    source_tag VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_raw_technology_signal_provider_signal
        UNIQUE (provider, provider_id, signal_type)
);

CREATE INDEX IF NOT EXISTS idx_raw_technology_signal_provider_tag_captured_at
    ON tech_talent_pulse.raw_technology_signal (provider, source_tag, captured_at DESC);
