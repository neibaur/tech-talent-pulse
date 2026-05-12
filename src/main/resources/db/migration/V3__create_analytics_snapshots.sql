CREATE TABLE IF NOT EXISTS tech_talent_pulse.technology_trend_snapshot (
    id UUID PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    tag VARCHAR(100) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    signal_count INTEGER NOT NULL,
    average_score DOUBLE PRECISION NOT NULL,
    average_answer_count DOUBLE PRECISION NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_technology_trend_snapshot_provider_tag_date
        UNIQUE (provider, tag, snapshot_date)
);

CREATE INDEX IF NOT EXISTS idx_technology_trend_snapshot_date_provider_tag
    ON tech_talent_pulse.technology_trend_snapshot (snapshot_date DESC, provider, tag);
