ALTER TABLE tech_talent_pulse.ingestion_run
    ADD COLUMN IF NOT EXISTS items_fetched INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS items_duplicate_skipped INTEGER NOT NULL DEFAULT 0;
